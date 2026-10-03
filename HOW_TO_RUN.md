# How to Run

## Prerequisites

| Tool | Version | Needed for |
|---|---|---|
| JDK | 21 | build, tests, run |
| Maven | 3.9+ | build (or build inside Docker) |
| Docker + Compose v2 | recent | Compose stack, image, MySQL integration test |
| kubectl + minikube / kind / Docker Desktop | recent | Kubernetes |
| make, bash, curl | any | Makefile, scripts (Windows: WSL2 or Git Bash) |

There is no Maven wrapper bundled. Run `mvn -N wrapper:wrapper` once if you want `./mvnw`.
The first build downloads AsciidoctorJ (JRuby), so allow a few minutes.

---

## 1. Generate and read the documentation

```bash
make docs          # mvn prepare-package: docs tests -> snippets -> HTML
make open-docs     # build + open target/generated-docs/index.html
```

| Path | Content |
|---|---|
| `target/generated-snippets/<operation-id>/` | raw snippets (curl, HTTP request/response, field tables, …) |
| `target/generated-docs/index.html` | rendered documentation |
| `target/classes/static/docs/index.html` | the same, packaged into the jar |

## 2. Run the application (H2, sample data)

```bash
make run
```

| URL | What |
|---|---|
| http://localhost:8080/docs/index.html | API documentation (public) |
| http://localhost:8080/api/v1/plans | public endpoint |
| http://localhost:8080/api/v1/customers | needs `agent:agent-pass` or `admin:admin-pass` |

```bash
curl -u agent:agent-pass http://localhost:8080/api/v1/customers/1
curl -u agent:agent-pass http://localhost:8080/api/v2/customers/1
```

## 3. Tests

```bash
make test          # unit + documentation tests
make verify        # + Asciidoctor, packaging, coverage check, served-docs check, integration tests
```

See [docs/testing.md](docs/testing.md). `MySqlFlywayIT` needs Docker and is skipped without it.

## 4. Docker Compose (MySQL + app, prod profile)

```bash
make up            # builds the image (runs the documentation tests inside), starts MySQL + app
make logs
make smoke         # uses the passwords from .env
make docs-static   # optional: static docs on nginx at http://localhost:8082 (like a docs host)
make down / make nuke
```

The demo passwords come from `.env` (`AGENT_PASSWORD`, `ADMIN_PASSWORD`). Change them there.

## 5. Kubernetes

```bash
minikube start && minikube addons enable ingress
make k8s-deploy
make k8s-port-forward      # http://localhost:8080/docs/index.html
make k8s-delete
```

The ingress host `telecom-api.local` serves both `/api/**` and `/docs/**`.

## 6. Publishing the docs

`.github/workflows/ci.yml` runs `mvn verify`, uploads `target/generated-docs` as an artifact, and on `main`
deploys it to **GitHub Pages**. Enable it in *Settings → Pages → Source: GitHub Actions*.

## 7. Optional OpenAPI 3 and Swagger UI

```bash
make openapi       # mvn -Popenapi clean verify
```

See [docs/openapi-integration.md](docs/openapi-integration.md). Check the versions there before you rely on this profile.

## Troubleshooting

| Symptom | Fix |
|---|---|
| Asciidoctor fails with "snippet ... not found" or another WARN | An `operation::` references an id no test produced. Check the id, or add the documentation test. Also happens with `-DskipTests` (add `-Dasciidoctor.skip=true`) |
| `SnippetException: Fields with the following paths were not documented` | The response gained a field. Document it in the test (or in `ApiDocs`) |
| `SnippetException: ... were not found in the payload` | A documented field no longer exists, or is null/absent. Remove it, or mark it `.optional()` with a `.type(...)` |
| `ApiDocumentationCoverageIT` lists endpoints | Write documentation tests for them |
| `/docs/index.html` returns 404 when running from the IDE | Run `make docs` once (the IDE doesn't run Asciidoctor) |
| `make smoke` gets 401 | The passwords differ. `make smoke` reads `.env`; locally the defaults are `agent-pass` / `admin-pass` |
| `MySqlFlywayIT` skipped | Docker isn't running |
