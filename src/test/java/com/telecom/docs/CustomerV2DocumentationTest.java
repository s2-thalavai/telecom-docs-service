package com.telecom.docs;

import com.telecom.config.SecurityConfig;
import com.telecom.controller.CustomerV2Controller;
import com.telecom.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** v2 is documented under its own identifier prefix: target/generated-snippets/v2/... */
@WebMvcTest(CustomerV2Controller.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.telecom.example", uriPort = 443)
@Import({SecurityConfig.class, RestDocsConfiguration.class})
class CustomerV2DocumentationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CustomerService service;

    @Test
    void getCustomerV2() throws Exception {
        when(service.getV2(42L)).thenReturn(ApiDocs.CUSTOMER_V2);

        mvc.perform(get("/api/v2/customers/{id}", 42L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD)))
           .andExpect(status().isOk())
           .andDo(document("v2/customer-get",
                   pathParameters(parameterWithName("id").description("Customer id")),
                   responseFields(
                           fieldWithPath("id").description("Customer id"),
                           fieldWithPath("name").description("Full name"),
                           fieldWithPath("status").description("`ACTIVE`, `SUSPENDED` or `TERMINATED`"),
                           fieldWithPath("contact.email").description("Email address"),
                           fieldWithPath("contact.msisdn").description("Mobile number"),
                           fieldWithPath("subscription").optional().type(JsonFieldType.OBJECT).description("Current plan; absent if none"),
                           fieldWithPath("subscription.planCode").optional().type(JsonFieldType.STRING).description("Plan code"),
                           fieldWithPath("subscription.planName").optional().type(JsonFieldType.STRING).description("Plan name"),
                           fieldWithPath("subscription.monthlyPrice").optional().type(JsonFieldType.NUMBER).description("Monthly rental in INR"),
                           fieldWithPath("memberSince").description("Creation time (ISO-8601, UTC)"))));
    }
}
