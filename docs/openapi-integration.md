# Optional: OpenAPI 3 and Swagger UI from the same tests

The `openapi` Maven profile adds a machine-readable OpenAPI 3 description, generated from REST Docs tests,
alongside the HTML documentation:

```bash
mvn -Popenapi clean verify
java -jar target/telecom-docs-service-1.0.0.jar
# http://localhost:8080/docs/openapi3.yaml  and  http://localhost:8080/swagger-ui.html
```

| Piece | Role |
|---|---|
| `com.epages:restdocs-api-spec-mockmvc` (test) | `MockMvcRestDocumentationWrapper.document(...)` + `resource(...)` write a `resource.json` per operation, next to the normal snippets |
| `src/openapi-test/java/.../OpenApiDocumentationTest` | example operations (plans list, customer get). Added as a test source only in this profile |
| `io.github.berkleytechnologyservices:restdocs-spec-maven-plugin` | converts the `resource.json` files into `static/docs/openapi3.yaml` in `prepare-package` |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | Swagger UI. `springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.url=/docs/openapi3.yaml` make it display the **test-generated** spec, not one derived from annotations |

**Before enabling it, check these:**

- Versions are properties in `pom.xml` (`restdocs-api-spec.version`, `restdocs-spec-maven-plugin.version`, `springdoc.version`). Set them to the latest releases compatible with Spring Boot 3.5.
- The Maven plugin is a community project. Confirm its parameter names (`specification`, `outputDirectory`, `filename`, `host`, …) against its README for the version you pick, and check the generated file name (`openapi3.yaml` vs `.yml`). Then adjust `springdoc.swagger-ui.url` to match.
- With Gradle, use the official `com.epages.restdocs-api-spec` plugin and its `openapi3` task instead.
- The default build doesn't use any of this. If the profile fails, the HTML docs are unaffected.

**Other ways to integrate:**

| Target | How |
|---|---|
| Postman / Insomnia | Import `openapi3.yaml` (restdocs-api-spec's Gradle plugin can also emit a Postman collection) |
| API gateways (Azure APIM, AWS API Gateway, Kong) | Import `openapi3.yaml` |
| GitHub Pages / internal docs host | The CI workflow already publishes `target/generated-docs` |
| MkDocs / Backstage TechDocs | Switch snippets to Markdown: `configurer.snippets().withTemplateFormat(TemplateFormats.markdown())` |
