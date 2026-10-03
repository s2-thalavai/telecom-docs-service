# Documenting an Endpoint – Checklist

The build enforces most of this list. If you skip a step, a test or the docs build fails and tells you where.

1. **Implement** the endpoint (controller, service, DTO with Bean Validation annotations).
2. **Add a documentation test** in `src/test/java/com/telecom/docs`:
   - `@WebMvcTest(YourController.class)`, `@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.telecom.example", uriPort = 443)`, `@Import({SecurityConfig.class, RestDocsConfiguration.class})`
   - mock the service with **fixed data** (ids, timestamps) from `ApiDocs`, so examples don't change between builds
   - build requests with `RestDocumentationRequestBuilders` (required for `pathParameters`)
   - authenticate with `httpBasic(...)`; the credentials are masked automatically
   - `document("<resource>-<action>", ...)` with `requestHeaders`, `pathParameters`, `queryParameters`, `requestFields` (use `ConstrainedFields` for the Constraints column), `responseHeaders`, `responseFields`
   - reuse descriptors from `ApiDocs` (`customer()`, `page()`, `problem()`); use `andWithPrefix` for nested/array content and `subsectionWithPath` for maps
3. **Document its errors.** Add a case to `ErrorDocumentationTest` if it returns a new kind of error.
4. **Reference it in the prose.** Add an anchor and an `operation::<id>[snippets='...']` block to the right `src/main/asciidoc/*.adoc`.
5. **New API version?** Use an identifier prefix (`v3/customer-get`) and a new `customers-v3.adoc` included from `index.adoc`.
6. Run `make verify`.

| You forgot | What fails |
|---|---|
| a response field (or renamed one) | the documentation test (`SnippetException`) |
| a query parameter in `queryParameters` | the documentation test |
| the documentation test entirely | `ApiDocumentationCoverageIT` ("Endpoints without REST Docs") |
| an `operation::` referencing an id no test produces | the Asciidoctor step (`failIf WARN`) |
| the anchor or section in the HTML | `DocsServedIT` (if you add the anchor to its checklist) |

## Conventions

| Item | Convention |
|---|---|
| Identifiers | `{resource}-{action}`, plural for collections (`customers-list`), `error-{kind}`, version prefix for v2+ |
| Example host | `https://api.telecom.example` |
| Sample data | `ApiDocs` constants. Customer 42 is "Arun Kumar" on plan `UNL-599` |
| Snippets shown | `curl-request`, then parameters and fields, then `http-response` |
| Custom template | `src/test/resources/org/springframework/restdocs/templates/asciidoctor/request-fields.snippet` (adds Optional and Constraints columns, so every request field must use `ConstrainedFields`) |
