# Running backend + frontend together locally with Docker Compose

This is the closest local reproduction of production: three containers
(Postgres, backend, frontend), wired together exactly like `docker-compose.prod.yml`
wires them on EC2 (minus the host-level Nginx and TLS).

## Prerequisites

- Docker Desktop (or Docker Engine + Compose plugin on Linux) installed and running.
- Ports `5432`, `8080`, and `8081` free on your machine.

## Step 1 — Build and start everything

From the repository root:

```bash
cd canteen-saarthi-java
docker compose up --build
```

First run builds both images (Maven build inside a container for the backend, npm
build inside a container for the frontend) — expect a few minutes. Subsequent runs
are much faster thanks to Docker layer caching.

Watch the logs for:

```
canteen-postgres  | ... database system is ready to accept connections
canteen-backend   | ... Flyway ... Successfully applied 2 migrations
canteen-backend   | ... Started CanteenApplication in X seconds
canteen-frontend  | ... nginx entered running state
```

## Step 2 — Verify each service

```bash
# Postgres is up and healthy
docker compose ps

# Backend directly
curl http://localhost:8080/api/healthz
curl http://localhost:8080/api/dashboard/summary

# Frontend, which proxies /api/* to the backend container internally
curl http://localhost:8081/api/healthz
```

Open **http://localhost:8081** in a browser — this is the full app, frontend and
backend wired together exactly as they will be behind the EC2 Nginx. Log in with
the seeded default account (change this immediately in any non-local environment —
see `.env.prod.example`):

```
Username: admin
Password: ChangeMe123!
```

Swagger UI for the API: **http://localhost:8080/swagger-ui.html**

## Step 3 — Run in the background / view logs / stop

```bash
docker compose up -d --build     # detached
docker compose logs -f backend   # tail one service's logs
docker compose logs -f           # tail everything
docker compose down              # stop, keep the Postgres volume (data persists)
docker compose down -v           # stop and wipe the Postgres volume (fresh DB next time)
```

## Step 4 — Rebuild after a code change

```bash
docker compose up --build backend     # rebuild just the backend
docker compose up --build frontend    # rebuild just the frontend
```

## Step 5 — Run the backend's automated tests inside Docker (optional)

You don't need Eclipse for this — Maven runs fine in a throwaway container too:

```bash
docker run --rm -v "$PWD/backend":/app -w /app maven:3.9-eclipse-temurin-17 \
  mvn test
```

Note: the Testcontainers-based integration test will try to start its own Postgres
container, so this requires Docker-in-Docker or running it on the host directly
(via Eclipse/Maven as in `ECLIPSE-LOCAL-TESTING.md`) rather than nested inside
another container.

## What's different from production

| Aspect | Local Compose | Production (EC2) |
|---|---|---|
| Entry point | `frontend:8081` directly | Host-level Nginx on `:80`/`:443` |
| TLS | None | Let's Encrypt via Certbot |
| Database | Postgres container, ephemeral volume | RDS or Supabase (durable, backed up) |
| Config source | Hardcoded env values in `docker-compose.yml` | `.env` file with real secrets |
| Rate limiting | None | Configured in `nginx/nginx.conf` |

## Common issues

| Symptom | Fix |
|---|---|
| `port is already allocated` | Something else is using 5432/8080/8081 — stop it or edit the `ports:` mapping in `docker-compose.yml` |
| Backend keeps restarting | `docker compose logs backend` — usually a Flyway migration or datasource config issue |
| Frontend loads but API calls fail with CORS errors | You're hitting the backend directly from a browser tab open on a different origin — go through `http://localhost:8081` instead, which proxies through Nginx same-origin |
| Stale data after schema changes | `docker compose down -v` to drop the Postgres volume, then `up --build` again |
