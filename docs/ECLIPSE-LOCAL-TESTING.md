# Testing the backend locally in Eclipse

## Prerequisites

- **Eclipse IDE for Enterprise Java and Web Developers** (2023-12 or newer) — this bundle
  includes the Maven (m2e) and Spring tooling you need. Plain "Eclipse IDE for Java
  Developers" also works if you install the **m2e** (Maven) plugin from the Eclipse
  Marketplace.
- **JDK 17** installed and registered in Eclipse (`Window > Preferences > Java >
  Installed JREs > Add...`, point it at your JDK 17 install).
- **Docker Desktop** (or Colima/Podman) running — used to start a local Postgres
  container and, optionally, for the Testcontainers-based integration test.
- **Postman or `curl`** for manually exercising endpoints (optional).

## Step 1 — Start a local Postgres database

The backend expects Postgres at `localhost:5432` when running with the `local` profile.
Easiest way: use the Postgres service defined in the repo's `docker-compose.yml`
without starting the other containers:

```bash
cd canteen-saarthi-java
docker compose up -d postgres
docker compose ps    # confirm postgres is "healthy"
```

This creates database `canteen_saarthi`, user `canteen_user`, password `canteen_pass`
(matches `application-local.yml` — change both together if you edit one).

## Step 2 — Import the backend project into Eclipse

1. `File > Import... > Maven > Existing Maven Projects`
2. Browse to `canteen-saarthi-java/backend`, select the `pom.xml`, click **Finish**.
3. Eclipse will resolve dependencies from Maven Central (first import takes a few
   minutes). Wait for the progress bar in the bottom-right to finish.
4. Right-click the project → `Maven > Update Project...` (check "Force Update") if you
   see red error markers after import.

## Step 3 — Run the application

**Option A — Run as Java Application (fastest for debugging):**

1. Open `src/main/java/com/csd/canteen/CanteenApplication.java`.
2. Right-click → `Run As > Java Application`.
3. It will start with the **default** profile, which has no datasource configured and
   will fail to connect. Instead, edit the run configuration:
   - `Run > Run Configurations... > Java Application > CanteenApplication`
   - Go to the **Arguments** tab → **VM arguments** → add:
     ```
     -Dspring.profiles.active=local
     ```
   - Click **Run**.
4. Console should show `Started CanteenApplication in X seconds` and Flyway logs
   showing `V1__init_schema.sql` and `V2__seed_data.sql` applied.

**Option B — Run via Maven (`mvn spring-boot:run`):**

1. Right-click the project → `Run As > Maven Build...`
2. Goals: `spring-boot:run`
3. Under the **JRE** tab or via a `-D` argument, set:
   ```
   -Dspring-boot.run.profiles=local
   ```
4. Click **Run**.

Either way, the API is now live at **http://localhost:8080**.

## Step 4 — Verify it's working

Most endpoints now require a JWT (see the README's "Authentication & roles"
section). Log in first with the seeded admin account, then use the token:

```bash
curl http://localhost:8080/api/healthz

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"ChangeMe123!"}' | jq -r .token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/dashboard/summary
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/inventory
```

(No `jq`? Just copy the `token` field out of the login response by hand.)

Or open **http://localhost:8080/swagger-ui.html** in a browser — click
**Authorize**, paste `Bearer <token>` (get the token from `/api/auth/login` via
the "Try it out" button on that endpoint first), and every other endpoint's
"Try it out" button will then include it automatically.

## Step 5 — Run the automated tests in Eclipse

The project has three layers of tests:

| Type | Location | What it needs |
|------|----------|----------------|
| Unit tests (Mockito) | `src/test/.../service/*Test.java` | Nothing extra — pure JVM |
| Controller slice tests (`@WebMvcTest`) | `src/test/.../controller/*Test.java` | Nothing extra |
| Integration test (Testcontainers) | `src/test/.../integration/*Test.java` | **Docker running** |

**Run all tests:**

1. Right-click the `backend` project → `Run As > Maven Test`. This runs the whole
   suite via Maven Surefire and prints a summary in the Console.
2. Or right-click any individual `*Test.java` file → `Run As > JUnit Test` to run just
   that class from Eclipse's built-in JUnit runner (nicer green/red bar UI).

If Docker isn't running, the Testcontainers-based
`DashboardApiIntegrationTest` will fail with a connection error — that's expected;
just start Docker and re-run, or skip it with:

```bash
mvn test -Dtest='!DashboardApiIntegrationTest'
```

## Step 6 — Debugging

Set breakpoints in any controller/service/repository class, then:

- `Run > Debug As > Java Application` (with the same `-Dspring.profiles.active=local`
  VM argument as above), or
- Right-click the run configuration you created → `Debug`.

Hit the endpoint from `curl`/Postman/the frontend and Eclipse will stop at your
breakpoint as usual.

## Common issues

| Symptom | Fix |
|---|---|
| `Connection refused` on startup | Postgres container isn't running — `docker compose up -d postgres` |
| `FlywayValidateException` / checksum mismatch | You edited an already-applied migration file. Either `docker compose down -v` to wipe the local DB and start fresh, or add a new `V3__...sql` migration instead of editing `V1`/`V2`. |
| Port `8080` already in use | Stop whatever else is using it, or run with `-Dserver.port=8090` |
| CORS errors when the frontend calls the API | Confirm `application-local.yml`'s `app.cors.allowed-origins` includes the frontend's origin (`http://localhost:5173` by default) |
