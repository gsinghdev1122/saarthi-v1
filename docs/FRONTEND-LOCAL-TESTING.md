# Testing the frontend locally (standalone)

The frontend is a completely separate deployable — its own `package.json`,
its own Dockerfile, its own dev server. You can develop and test it while the
backend runs from Eclipse, from `docker-compose`, or from a plain terminal.

## Prerequisites

- **Node.js 20+** and **npm** installed (check with `node -v`).
- The backend running and reachable at `http://localhost:8080` (see
  `ECLIPSE-LOCAL-TESTING.md`, or just run `docker compose up -d postgres backend`
  from the repo root).

## Step 1 — Install dependencies

```bash
cd canteen-saarthi-java/frontend
npm install
```

## Step 2 — Run the dev server

```bash
npm run dev
```

This starts Vite on **http://localhost:5173**. The dev server proxies any request
to `/api/*` through to `http://localhost:8080` (configured in `vite.config.ts`), so
the browser never sees a cross-origin request — no CORS setup needed for this mode.

Open **http://localhost:5173** and you should see the dashboard load real data from
the backend.

If your backend is running somewhere other than `localhost:8080` (e.g. a different
port), override the proxy target:

```bash
VITE_API_PROXY_TARGET=http://localhost:9090 npm run dev
```

## Step 3 — Run the frontend test suite

```bash
npm run test
```

This runs the Vitest suite (`src/__tests__/*.test.tsx`) with jsdom, mocking the API
client so no backend is required for these tests to pass.

## Step 4 — Type-check and lint

```bash
npx tsc -b       # type-check only, no emit
npm run lint
```

## Step 5 — Build the production bundle locally

```bash
npm run build      # outputs to frontend/dist
npm run preview    # serves the built bundle on http://localhost:4173
```

`npm run preview` does **not** proxy `/api` calls automatically the way `npm run dev`
does — it serves the static bundle exactly as production will. To test the production
build against a real API, either:

- Run it via Docker (see `DOCKER-LOCAL-TESTING.md`), where the frontend's own Nginx
  container proxies `/api` to the backend container, or
- Temporarily add a proxy to `vite.config.ts`'s `preview` section, or
- Serve it behind the host-level `nginx/nginx.conf` used in production.

## Common issues

| Symptom | Fix |
|---|---|
| Blank page / network errors in devtools | Confirm the backend is running: `curl http://localhost:8080/api/healthz` |
| `ERR_CONNECTION_REFUSED` on `/api/...` in `npm run dev` | Set `VITE_API_PROXY_TARGET` to the backend's actual host:port |
| Type errors on `npm run build` | Run `npx tsc -b` for a focused error list separate from the Vite bundling step |
