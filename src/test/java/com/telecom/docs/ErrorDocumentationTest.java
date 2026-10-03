package com.telecom.docs;

import com.telecom.config.SecurityConfig;
import com.telecom.controller.CustomerController;
import com.telecom.exception.DuplicateResourceException;
import com.telecom.exception.InvalidStateException;
import com.telecom.exception.ResourceNotFoundException;
import com.telecom.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Real examples for every error the API returns. */
@WebMvcTest(CustomerController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.telecom.example", uriPort = 443)
@Import({SecurityConfig.class, RestDocsConfiguration.class})
class ErrorDocumentationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CustomerService service;

    @Test
    void validationError() throws Exception {
        mvc.perform(post("/api/v1/customers").with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\",\"msisdn\":\"12345\",\"planCode\":\"UNL-599\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.email").exists())
           .andDo(document("error-validation",
                   responseFields(ApiDocs.problem())
                           .and(subsectionWithPath("errors").description("Map of invalid field name to message"))));
        verifyNoInteractions(service);
    }

    @Test
    void notFound() throws Exception {
        when(service.get(999L)).thenThrow(new ResourceNotFoundException("Customer not found with id 999"));

        mvc.perform(get("/api/v1/customers/{id}", 999L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD)))
           .andExpect(status().isNotFound())
           .andDo(document("error-not-found", responseFields(ApiDocs.problem())));
    }

    @Test
    void duplicate() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateResourceException("Mobile number already registered: 9876543210"));

        mvc.perform(post("/api/v1/customers").with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Arun\",\"email\":\"a2@example.com\",\"msisdn\":\"9876543210\",\"planCode\":\"UNL-599\"}"))
           .andExpect(status().isConflict())
           .andDo(document("error-duplicate", responseFields(ApiDocs.problem())));
    }

    @Test
    void invalidStateTransition() throws Exception {
        when(service.changeStatus(eq(42L), any()))
                .thenThrow(new InvalidStateException("Cannot change status from TERMINATED to ACTIVE"));

        mvc.perform(patch("/api/v1/customers/{id}/status", 42L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
           .andExpect(status().isConflict())
           .andDo(document("error-invalid-state", responseFields(ApiDocs.problem())));
    }

    @Test
    void unauthorized() throws Exception {
        mvc.perform(get("/api/v1/customers/{id}", 42L))
           .andExpect(status().isUnauthorized())
           .andDo(document("error-unauthorized",
                   responseHeaders(headerWithName(HttpHeaders.WWW_AUTHENTICATE).description("Authentication scheme to use")),
                   responseFields(ApiDocs.problem())));
        verifyNoInteractions(service);
    }

    @Test
    void forbidden() throws Exception {
        mvc.perform(delete("/api/v1/customers/{id}", 42L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD)))
           .andExpect(status().isForbidden())
           .andDo(document("error-forbidden", responseFields(ApiDocs.problem())));
        verifyNoInteractions(service);
    }
}
