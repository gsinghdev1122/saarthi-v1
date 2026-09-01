# Canteen SAARTHI — Java/Spring Boot backend + React frontend

Production-track rebuild of the CSD Canteen Management prototype as two independently
deployable services:

- **`backend/`** — Spring Boot 3 (Java 17) REST API, PostgreSQL + Flyway, JUnit/Mockito/Testcontainers tests.
- **`frontend/`** — React + TypeScript + Vite SPA, served by its own Nginx container in production.
- **`nginx/`** — host-level Nginx reverse proxy config for the EC2 deployment.
- **`docker-compose.yml`** — local dev stack (Postgres + backend + frontend).
- **`docker-compose.prod.yml`** — EC2 production stack (backend + frontend + reverse-proxy Nginx).

## Read these in order

1. **[docs/ECLIPSE-LOCAL-TESTING.md](docs/ECLIPSE-LOCAL-TESTING.md)** — import the backend into Eclipse, run/debug it, run the test suite.
2. **[docs/FRONTEND-LOCAL-TESTING.md](docs/FRONTEND-LOCAL-TESTING.md)** — run the React app standalone against the backend.
3. **[docs/DOCKER-LOCAL-TESTING.md](docs/DOCKER-LOCAL-TESTING.md)** — run both services together with Docker Compose (closest to production).
4. **[docs/AWS-EC2-DEPLOYMENT.md](docs/AWS-EC2-DEPLOYMENT.md)** — deploy the Docker Compose stack to a free-tier EC2 instance behind Nginx, with HTTPS.
5. **[docs/SUPABASE-SETUP.md](docs/SUPABASE-SETUP.md)** — use a free Supabase Postgres instance instead of self-hosting the database.
6. **[docs/CI-PIPELINE.md](docs/CI-PIPELINE.md)** — what the GitHub Actions workflow does and how to finish wiring it up.

## Authentication & roles

The backend issues JWTs (`POST /api/auth/login`) and enforces role checks server-side
via Spring Security `@PreAuthorize`. There is **no public self-registration** —
accounts are created by an admin via `POST /api/users`.

On first startup, if the `app_users` table is empty, a default `admin` account is
seeded automatically (see `UserSeeder.java`) using `ADMIN_USERNAME`/`ADMIN_PASSWORD`
(defaults: `admin` / `ChangeMe123!` locally — **override both in `.env` for any
non-local deployment**, see `.env.prod.example`). Log in with it once, create real
named accounts, and treat it as a break-glass account afterward.

| Role | Can write | Read access |
|---|---|---|
| `ADMIN` | Everything, incl. user management | Everything |
| `CANTEEN_MANAGER` | Imports, inventory (via import), workforce, expenses | Everything |
| `STORE_SUPERVISOR` | Imports, inventory (via import) | Everything |
| `FINANCE_REVIEWER` | Expenses | Everything |
| `AUDITOR` | Nothing | Everything |

## CIMS file imports

`POST /api/imports/upload` (multipart) actually parses uploaded files rather than
just recording metadata:

- **`.prn`** fixed-width inventory/sales exports → parsed and upserted into the
  `inventory` table (see `CimsFileParser.INVENTORY_COLUMNS` for the exact byte
  offsets — calibrate these against a real CIMS sample file, they're a documented
  default).
- **`.xls`/`.xlsx`** attendance exports → parsed (flexible header matching) and
  applied to matching employees' attendance percentage.

`POST /api/imports` (JSON body) is kept for metadata-only registration when there's
no physical file to hand.

## Architecture at a glance

```
Browser
   │
   ▼
[EC2 Nginx :80/:443]  ── TLS termination, rate limiting, reverse proxy
   │              │
   ▼              ▼
[frontend:80]  [backend:8080]  ── Spring Boot REST API
   │ (proxies /api)             │
   └────────────►───────────────┘
                                 ▼
                        [PostgreSQL]  ── self-hosted container, RDS, or Supabase
```

Locally, the same shape is reproduced with `docker-compose.yml` (Postgres runs in a
container instead of RDS/Supabase), or you can run the backend and frontend as
plain local processes for fast iteration (see docs 1 and 2).

## API surface

| Method | Path                     | Auth                              | Purpose                                   |
|--------|--------------------------|------------------------------------|--------------------------------------------|
| POST   | `/api/auth/login`        | Public                             | Exchange username/password for a JWT      |
| GET/POST | `/api/users`            | ADMIN                              | List/create desk logins                   |
| GET    | `/api/healthz`           | Public                             | Liveness check                            |
| GET    | `/api/dashboard/summary` | Any authenticated role             | Dashboard KPIs                            |
| GET    | `/api/activity`          | Any authenticated role             | Recent activity feed (latest 12)          |
| GET    | `/api/imports`           | Any authenticated role             | List recent import batches                |
| POST   | `/api/imports`           | ADMIN, CANTEEN_MANAGER, STORE_SUPERVISOR | Register a batch (metadata only)    |
| POST   | `/api/imports/upload`    | ADMIN, CANTEEN_MANAGER, STORE_SUPERVISOR | Upload + parse a real `.prn`/`.xls` file |
| GET    | `/api/inventory`         | Any authenticated role             | List/search inventory (`division`, `search` query params) |
| GET    | `/api/employees`         | Any authenticated role             | List workforce                            |
| POST   | `/api/employees`         | ADMIN, CANTEEN_MANAGER             | Add a workforce member                    |
| GET    | `/api/expenses`          | Any authenticated role             | List expenses                             |
| POST   | `/api/expenses`          | ADMIN, CANTEEN_MANAGER, FINANCE_REVIEWER | Record an expense (auto-raises an approval) |
| GET    | `/api/approvals`         | Any authenticated role             | List the approval queue                   |
| GET    | `/api/reports/overview`  | Any authenticated role             | Sales/expense/profit summary              |

Full interactive docs (Swagger UI) are served at `/swagger-ui.html` whenever the
backend is running, and the raw OpenAPI JSON is at `/v3/api-docs` (both public, no
token needed, since they only describe the API shape).

## Known scope / what's intentionally left out

- **No real sales-import pipeline yet** — sales figures on the dashboard/reports
  (`DashboardService`, `ReportsService`) are still placeholders; only inventory and
  attendance imports are wired to real parsing so far. Grocery/liquor `.prn` sales
  files use the *same* parser as inventory (`PRN_FILE_TYPES` in `ImportService`) —
  extend `CimsFileParser` with a sales-specific row shape when you have a sample file.
- **`.prn` column offsets are a documented default**, not verified against a real
  CIMS export — recalibrate `CimsFileParser.INVENTORY_COLUMNS` once you have one.
- **No self-registration** — new accounts are created by an admin, by design in this
  defense/paramilitary context.
- **No password reset flow yet** — an admin currently has to create a new account or
  update the password hash directly; add a reset-token flow before this matters for
  a real deployment.
- **No refresh tokens** — JWTs are long-lived (`APP_JWT_EXPIRATION_MINUTES`, default
  8 hours) rather than short-lived-with-refresh; fine for an internal desk tool,
  worth revisiting if this ever faces the public internet.
"# saarthi-v1" 
