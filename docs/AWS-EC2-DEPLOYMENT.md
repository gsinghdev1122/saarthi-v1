# Deploying to AWS EC2 (free tier) with Docker + Nginx

This walks through taking the Docker Compose stack you tested locally and running it
on a single free-tier EC2 instance, fronted by Nginx with HTTPS.

## Architecture

```
Internet ──► EC2 Security Group (80, 443, 22) ──► Nginx container (:80/:443)
                                                        │
                                       ┌────────────────┼────────────────┐
                                       ▼                                 ▼
                              frontend container (:80)         backend container (:8080)
                                                                         │
                                                                         ▼
                                                        Postgres: RDS free tier, Supabase,
                                                        or a Postgres container on the
                                                        same box (see options below)
```

## Step 1 — Launch the EC2 instance

1. AWS Console → **EC2** → **Launch instance**.
2. **Name**: `canteen-saarthi`.
3. **AMI**: Ubuntu Server 24.04 LTS (free-tier eligible).
4. **Instance type**: `t2.micro` or `t3.micro` (both free-tier eligible in your first
   12 months; `t3.micro` if available in your region is a bit faster for the same
   price bracket).
5. **Key pair**: create a new one, download the `.pem`, keep it safe — you'll need it
   to SSH in.
6. **Network settings → Security group**: allow inbound:
   - SSH (22) — restrict to your IP if possible
   - HTTP (80) — from anywhere
   - HTTPS (443) — from anywhere
7. **Storage**: 20–30 GB gp3 is comfortable (free tier includes up to 30 GB).
8. Launch.

## Step 2 — Point a domain at it (recommended, needed for HTTPS)

In your DNS provider, create an **A record** for your domain/subdomain (e.g.
`canteen.yourdomain.com`) pointing at the EC2 instance's **public IPv4 address**
(use an Elastic IP so it doesn't change on reboot — EC2 → Elastic IPs → Allocate,
then Associate with your instance).

## Step 3 — SSH in and install Docker

```bash
ssh -i your-key.pem ubuntu@your-ec2-public-ip
```

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y ca-certificates curl gnupg git

# Install Docker Engine + Compose plugin (official Docker repo)
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Let your user run docker without sudo
sudo usermod -aG docker $USER
newgrp docker

docker --version
docker compose version
```

## Step 4 — Get the code onto the instance

Simplest: push this repo to a Git remote (GitHub/GitLab) and clone it on the box.

```bash
git clone https://github.com/your-org/canteen-saarthi-java.git
cd canteen-saarthi-java
```

(No Git remote yet? `scp -i your-key.pem -r canteen-saarthi-java ubuntu@your-ec2-ip:~`
from your laptop works too, just slower to iterate on.)

## Step 5 — Choose your database

Pick one:

- **Supabase (recommended for free tier + simplicity)** — see
  `docs/SUPABASE-SETUP.md`. No extra containers to manage, automatic backups on
  Supabase's side.
- **AWS RDS free tier** — `db.t3.micro`/`db.t4g.micro` Postgres, 20 GB storage, free
  for 12 months. Create it in the RDS console, put it in the same VPC as your EC2
  instance, and allow inbound Postgres (5432) from the EC2 instance's security group
  only (not "anywhere"). Then use its endpoint as `DB_HOST` below.
- **Self-hosted Postgres container on the same EC2 box** — cheapest, but you own
  backups/patching. Add a `postgres` service to `docker-compose.prod.yml` (copy the
  one from `docker-compose.yml`) with a named volume, and point `DB_HOST=postgres`.

Whichever you choose, you just need a host, port, db name, username, and password.

## Step 6 — Configure environment variables

```bash
cp .env.prod.example .env
nano .env
```

Fill in real values:

```env
DB_HOST=...
DB_PORT=5432
DB_NAME=canteen_saarthi
DB_USERNAME=...
DB_PASSWORD=...
DB_SSL_MODE=require
DB_POOL_SIZE=10
APP_CORS_ALLOWED_ORIGINS=https://canteen.yourdomain.com
SERVER_PORT=8080
```

`.env` is already gitignored-by-convention — never commit it. Set restrictive
permissions:

```bash
chmod 600 .env
```

## Step 7 — Bring the stack up (HTTP first)

```bash
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # watch Flyway + startup
```

Verify:

```bash
curl http://your-ec2-public-ip/healthz
curl http://your-ec2-public-ip/api/dashboard/summary
```

Or from your laptop browser: `http://canteen.yourdomain.com` (once DNS has
propagated) — you should see the full app working over plain HTTP.

## Step 8 — Add HTTPS with Let's Encrypt (Certbot)

The `nginx` container needs a real certificate mounted into `nginx/certs/`. The
simplest approach is to run Certbot's standalone mode briefly (stopping the Nginx
container for a few seconds) to obtain the cert, then wire it in permanently:

```bash
sudo apt install -y certbot

# Temporarily stop the nginx container so certbot can bind port 80 itself
docker compose -f docker-compose.prod.yml stop nginx

sudo certbot certonly --standalone \
  -d canteen.yourdomain.com \
  --agree-tos -m you@yourdomain.com --non-interactive

# Certs land in /etc/letsencrypt/live/canteen.yourdomain.com/
sudo cp /etc/letsencrypt/live/canteen.yourdomain.com/fullchain.pem nginx/certs/
sudo cp /etc/letsencrypt/live/canteen.yourdomain.com/privkey.pem nginx/certs/
sudo chown $USER:$USER nginx/certs/*.pem
```

Now edit `nginx/nginx.conf`:

1. Uncomment the `server { listen 443 ssl; ... }` block at the bottom and set
   `server_name canteen.yourdomain.com;`.
2. Change the `listen 80` server block's `location /` (and everywhere else) to
   redirect to HTTPS instead of proxying directly:

```nginx
server {
    listen 80;
    server_name canteen.yourdomain.com;
    location /healthz { proxy_pass http://backend_upstream/api/healthz; }
    location / { return 301 https://$host$request_uri; }
}
```

(Keep `/healthz` on plain HTTP so simple monitoring tools that don't do TLS can still
hit it — everything else redirects.)

Restart:

```bash
docker compose -f docker-compose.prod.yml up -d nginx
```

**Auto-renewal** (certs expire every 90 days): add a cron job that renews and
re-copies the cert, then reloads Nginx:

```bash
sudo crontab -e
```

Add:

```
0 3 * * * certbot renew --quiet --pre-hook "docker compose -f /home/ubuntu/canteen-saarthi-java/docker-compose.prod.yml stop nginx" --post-hook "cp /etc/letsencrypt/live/canteen.yourdomain.com/*.pem /home/ubuntu/canteen-saarthi-java/nginx/certs/ && docker compose -f /home/ubuntu/canteen-saarthi-java/docker-compose.prod.yml up -d nginx"
```

## Step 9 — Update `APP_CORS_ALLOWED_ORIGINS` for HTTPS

Make sure `.env`'s `APP_CORS_ALLOWED_ORIGINS` uses `https://` once TLS is live, then:

```bash
docker compose -f docker-compose.prod.yml up -d backend
```

## Step 10 — Redeploying after a code change

```bash
cd canteen-saarthi-java
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

This rebuilds only the images whose source changed and restarts those containers
with minimal downtime.

## Operational basics

```bash
# Logs
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f nginx

# Restart one service
docker compose -f docker-compose.prod.yml restart backend

# Resource usage (t2/t3.micro has 1 GB RAM — keep an eye on this)
docker stats

# Disk cleanup (old images pile up after repeated builds)
docker image prune -f
```

## Production hardening checklist (do before real users touch this)

These mirror the checklist already called out in the original prototype's
`docs/BUILD-AND-DEPLOY.md` — most are still open for the Java rewrite. Auth/RBAC
(the first two items below) are now implemented — see the README's "Authentication
& roles" section — but still need the operational steps below before go-live:

- [x] ~~Add authentication~~ — done (Spring Security + JWT, see README).
- [x] ~~Add RBAC roles~~ — done (ADMIN / CANTEEN_MANAGER / STORE_SUPERVISOR /
      FINANCE_REVIEWER / AUDITOR, enforced via `@PreAuthorize`).
- [ ] Set `APP_JWT_SECRET` and `ADMIN_PASSWORD` to real, random values in `.env` —
      the defaults in `application.yml` are for local dev only (generate a secret
      with `openssl rand -base64 48`).
- [ ] Log in with the seeded admin account once, create real named accounts for
      each operator, and stop using the seeded admin for daily work.
- [ ] Add a password reset / rotation flow before this is anyone's only way in.
- [ ] Add tenant/canteen scoping to every query once multiple canteens are supported.
- [ ] Move uploaded source files to S3 instead of storing bytes/metadata only in
      Postgres (build out the real import parser — see the README's "Known scope").
- [ ] Tighten the EC2 security group: restrict SSH to your IP, consider AWS Systems
      Manager Session Manager instead of open SSH entirely.
- [ ] Set up CloudWatch (or a free alternative like an Uptime Robot check against
      `/healthz`) for alerting.
- [ ] Automate backups for whichever Postgres option you chose (RDS automated
      backups, Supabase backups, or `pg_dump` cron for a self-hosted container).
- [ ] Load test before go-live (`hey`, `k6`, or `ab` against `/api/dashboard/summary`
      and `/api/inventory`).
- [ ] Run `docker scan` / Trivy against both images for known CVEs periodically.
- [ ] Rotate the EC2 key pair and `.env` secrets on a schedule; never commit `.env`.

## Cost notes (free tier)

- `t2.micro`/`t3.micro` EC2: free for 750 hrs/month for the first 12 months, then
  standard hourly billing (~$7–8/month for `t3.micro` in most US regions).
- Elastic IP: free while associated with a running instance; charged if left
  unassociated.
- RDS free tier: `db.t3.micro`/`db.t4g.micro`, 20 GB, free for 12 months, then billed.
- Supabase free tier: no time limit, but has project pause-after-inactivity and
  storage/bandwidth caps — fine for a small internal tool, check current limits at
  supabase.com/pricing before committing to it for anything business-critical.
