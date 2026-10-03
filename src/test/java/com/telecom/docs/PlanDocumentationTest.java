package com.telecom.docs;

import com.telecom.config.SecurityConfig;
import com.telecom.controller.PlanController;
import com.telecom.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.telecom.example", uriPort = 443)
@Import({SecurityConfig.class, RestDocsConfiguration.class})
class PlanDocumentationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlanService planService;

    @Test
    void listPlans() throws Exception {
        when(planService.listActive()).thenReturn(List.of(ApiDocs.DATA, ApiDocs.UNLIMITED));

        mvc.perform(get("/api/v1/plans"))                                    // public: no credentials
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].code").value("DATA-299"))
           .andDo(document("plans-list",
                   responseFields(fieldWithPath("[]").description("Active plans, cheapest first"))
                           .andWithPrefix("[].", ApiDocs.plan())));
    }

    @Test
    void getPlan() throws Exception {
        when(planService.get("UNL-599")).thenReturn(ApiDocs.UNLIMITED);

        mvc.perform(get("/api/v1/plans/{code}", "UNL-599"))
           .andExpect(status().isOk())
           .andDo(document("plan-get",
                   pathParameters(parameterWithName("code").description("Plan code, e.g. `UNL-599`")),
                   responseFields(ApiDocs.plan())));
    }
}
