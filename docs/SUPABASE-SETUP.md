# Using Supabase Postgres instead of self-hosting the database

Supabase's free tier gives you a managed PostgreSQL instance, which removes the need
to run/patch/back up Postgres yourself on the EC2 box. The Spring Boot backend
doesn't care which Postgres it talks to — this is purely a connection-string swap.

## Step 1 — Create the project

1. Go to https://supabase.com and sign up / log in.
2. **New project** → choose an organization, name it (e.g. `canteen-saarthi`), set a
   strong database password (save it somewhere safe — you'll need it below), pick the
   region closest to your EC2 instance (lower latency), and create the project.
3. Wait ~2 minutes for provisioning.

## Step 2 — Get the connection details

1. In the project, go to **Project Settings → Database**.
2. Under **Connection info**, note:
   - **Host** (e.g. `db.xxxxxxxxxxxx.supabase.co`)
   - **Port** — use **6543** (the pooled "Transaction" pooler) for the app's normal
     traffic, or **5432** (direct connection) if you prefer no pooler. For a small
     app either works; the pooler is recommended if you expect many short-lived
     connections.
   - **Database name** — `postgres` by default.
   - **User** — `postgres` by default.
   - **Password** — the one you set in Step 1.
3. Supabase requires SSL — keep that in mind for the `sslmode` setting below.

## Step 3 — Point the backend at Supabase

The `prod` Spring profile already supports this via environment variables (see
`backend/src/main/resources/application-prod.yml`). Fill in `.env` on the EC2 host
(copied from `.env.prod.example`) like this:

```env
DB_HOST=db.xxxxxxxxxxxx.supabase.co
DB_PORT=6543
DB_NAME=postgres
DB_USERNAME=postgres
DB_PASSWORD=your-supabase-db-password
DB_SSL_MODE=require
DB_POOL_SIZE=10
```

That's it — no code changes needed. `docker-compose.prod.yml` reads `.env` and
injects these into the backend container.

## Step 4 — Let Flyway create the schema

You don't need to run any SQL by hand in the Supabase SQL editor. On first startup,
Flyway (bundled in the backend) will automatically apply
`V1__init_schema.sql` and `V2__seed_data.sql` against the Supabase database, exactly
as it does locally against the Dockerized Postgres. Just start the backend once
(`docker compose -f docker-compose.prod.yml up -d backend` or the whole stack) and
watch the logs for:

```
Flyway ... Successfully applied 2 migrations to schema "public"
```

You can then browse the tables in **Supabase Studio → Table Editor** to confirm the
seed data is there.

## Step 5 — Verify from the EC2 host

```bash
docker compose -f docker-compose.prod.yml logs backend | grep -i flyway
curl http://localhost:8080/api/dashboard/summary   # if backend port is exposed for testing
```

## Local testing against Supabase (optional)

You can also point your **local** `application-local.yml` at Supabase instead of a
local Postgres container if you want your Eclipse-run backend to share data with a
teammate. Just swap the `spring.datasource.*` values in
`backend/src/main/resources/application-local.yml` to the Supabase host/port/
credentials with `?sslmode=require` appended to the URL. Not recommended for daily
development (slower feedback loop, shared mutable state) — better for a quick sanity
check before deploying.

## Notes and gotchas

- **Connection limits**: Supabase free tier caps concurrent direct connections; the
  pooled port (6543) is designed for exactly this kind of many-short-connections
  workload from a backend with a HikariCP pool. Keep `DB_POOL_SIZE` modest (5–10).
- **Backups**: Supabase free tier has limited automatic backup retention — for
  anything you can't afford to lose, export manually via **Database → Backups** or
  `pg_dump` periodically.
- **Row-level security (RLS)**: Supabase enables RLS features you can opt into, but
  since this backend connects as the `postgres` superuser-equivalent role directly
  (not through Supabase's PostgREST/Auth layer), RLS policies won't apply to it by
  default — access control for this app happens in the Spring Boot layer instead
  (see the auth hardening note in `AWS-EC2-DEPLOYMENT.md`).
