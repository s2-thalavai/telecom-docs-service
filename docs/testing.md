# Testing Strategy

| Layer | Naming | Phase | Command |
|---|---|---|---|
| Unit + documentation tests | `*Test.java` | `test` (Surefire) | `make test` |
| Asciidoctor rendering, copy to `static/docs` | – | `prepare-package` | `make docs` |
| Integration and consistency checks | `*IT.java` | `integration-test` (Failsafe) | `make verify` |

The order matters. Documentation tests write `target/generated-snippets`. Asciidoctor turns them into HTML and fails on any missing snippet. The HTML is copied into `target/classes/static/docs`. Only then do the ITs check coverage and the served docs.

## Documentation tests (`com.telecom.docs`)

| Class | Operations documented |
|---|---|
| `PlanDocumentationTest` | `plans-list`, `plan-get` (public, no credentials) |
| `CustomerDocumentationTest` | `customer-create`, `customer-get`, `customers-list`, `customer-update`, `customer-status-change`, `customer-delete` |
| `CustomerV2DocumentationTest` | `v2/customer-get` |
| `ErrorDocumentationTest` | `error-validation`, `error-not-found`, `error-duplicate`, `error-invalid-state`, `error-unauthorized`, `error-forbidden` |
| `MaskAuthorizationHeaderTest` | credentials masked, placeholder still decodable by the curl/HTTPie snippets, other headers kept |

These tests also assert status codes and key response values, so they double as web-layer tests.

## Unit tests

| Class | Covers |
|---|---|
| `CustomerStatusTest` | the full status transition matrix |
| `CustomerMapperTest` | v1 and v2 representations, customers without a plan, unlimited minutes |
| `CustomerServiceTest` | create, duplicates, unknown plan, status rules, update conflicts, list filter, not found |
| `SecurityRulesTest` | the access matrix from overview.adoc: public plans, 401 with `WWW-Authenticate` and ProblemDetail, agent 403 on delete, admin delete, deny-all for unknown paths |

## Integration and consistency checks

| Class | Covers |
|---|---|
| `ApiDocumentationCoverageIT` | every `/api/**` handler mapping (method + path pattern) has a generated `http-request` example |
| `DocsServedIT` | `/docs/index.html` is served without credentials and contains the key sections, the public host and masked credentials, and never the real password |
| `CustomerApiIT` | the full stack (H2, real security) behaves as documented: lifecycle, v2, update, errors in ProblemDetail format, paging cap |
| `MySqlFlywayIT` | prod profile on MySQL 8.4 via Testcontainers: Flyway schema and seed. Skipped without Docker |
