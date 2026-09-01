# Continuous Integration (GitHub Actions)

`.github/workflows/ci.yml` runs on every push and pull request to `main`, with
three jobs:

| Job | What it does |
|---|---|
| `backend` | JDK 17 + Maven cache → `mvn test` (runs the full suite: unit, `@WebMvcTest` slice, and Testcontainers integration tests — GitHub's `ubuntu-latest` runners have Docker preinstalled, so Testcontainers works with no extra setup) → builds the jar → uploads it and the test reports as artifacts |
| `frontend` | Node 20 → `npm install` → `tsc -b` (type-check) → `npm run test` (Vitest) → `npm run build` → uploads `dist/` as an artifact |
| `docker-build` | Builds both the backend and frontend Docker images (validation only, no push to a registry) using `docker/build-push-action` with GitHub Actions layer caching — catches Dockerfile/build-context breakage before anyone deploys |

`docker-build` depends on both `backend` and `frontend` passing first.

## First-time setup after cloning

The frontend job currently runs `npm install` (not `npm ci`) because this repo
doesn't ship a `package-lock.json` yet — it wasn't generated in the environment
this project was built in (no network access). Do this once, locally:

```bash
cd frontend
npm install
git add package-lock.json
git commit -m "Add frontend lockfile"
```

Then switch the frontend job in `ci.yml` to the faster, fully reproducible
`npm ci` + npm cache, following the comment left in that file:

```yaml
      - name: Set up Node.js 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: frontend/package-lock.json
      ...
      - name: Install dependencies
        run: npm ci
```

## Extending the pipeline later

Natural next additions once you're ready:

- **Push images to a registry** (Docker Hub, GHCR, or ECR) and add a `deploy`
  job that SSHes into the EC2 host and runs `docker compose -f
  docker-compose.prod.yml pull && up -d` — turns `docs/AWS-EC2-DEPLOYMENT.md`'s
  manual redeploy step into a one-click/automatic one.
- **Dependabot** (`.github/dependabot.yml`) for automated dependency update PRs
  on both `backend/pom.xml` and `frontend/package.json`.
- **Static analysis**: `mvn spotbugs:check` / `npm audit` as extra CI steps,
  or a CodeQL workflow from GitHub's default security templates.
- **Branch protection**: require the three CI jobs to pass before merging to
  `main` (Settings → Branches → Branch protection rules in GitHub).
