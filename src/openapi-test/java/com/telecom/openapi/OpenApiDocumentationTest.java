package com.telecom.openapi;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.telecom.config.SecurityConfig;
import com.telecom.controller.CustomerController;
import com.telecom.controller.PlanController;
import com.telecom.docs.ApiDocs;
import com.telecom.docs.RestDocsConfiguration;
import com.telecom.service.CustomerService;
import com.telecom.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Same REST Docs tests, wrapped by restdocs-api-spec: besides the usual snippets each operation writes a
 * resource.json, which restdocs-spec-maven-plugin turns into static/docs/openapi3.yaml (Swagger UI).
 * Only compiled with -Popenapi. Extend it to every operation you want in the OpenAPI file.
 */
@WebMvcTest({PlanController.class, CustomerController.class})
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.telecom.example", uriPort = 443)
@Import({SecurityConfig.class, RestDocsConfiguration.class})
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlanService planService;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void plans() throws Exception {
        when(planService.listActive()).thenReturn(List.of(ApiDocs.DATA, ApiDocs.UNLIMITED));

        mvc.perform(get("/api/v1/plans"))
           .andExpect(status().isOk())
           .andDo(document("openapi/plans-list", resource(ResourceSnippetParameters.builder()
                   .tag("Plans")
                   .summary("List active plans")
                   .description("Returns all active plans, cheapest first. Public.")
                   .responseSchema(Schema.schema("PlanList"))
                   .responseFields(withArrayItems())
                   .build())));
    }

    @Test
    void getCustomer() throws Exception {
        when(customerService.get(42L)).thenReturn(ApiDocs.CUSTOMER);

        mvc.perform(get("/api/v1/customers/{id}", 42L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD)))
           .andExpect(status().isOk())
           .andDo(document("openapi/customer-get", resource(ResourceSnippetParameters.builder()
                   .tag("Customers")
                   .summary("Get a customer")
                   .requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("HTTP Basic credentials"))
                   .pathParameters(parameterWithName("id").description("Customer id"))
                   .responseSchema(Schema.schema("CustomerResponse"))
                   .responseFields(ApiDocs.customer())
                   .build())));
    }

    /** "[]" plus every plan field prefixed with "[]." - one list, because the builder keeps the last call. */
    private static FieldDescriptor[] withArrayItems() {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(fieldWithPath("[]").description("Active plans"));
        fields.addAll(PayloadDocumentation.applyPathPrefix("[].", List.of(ApiDocs.plan())));
        return fields.toArray(new FieldDescriptor[0]);
    }
}
