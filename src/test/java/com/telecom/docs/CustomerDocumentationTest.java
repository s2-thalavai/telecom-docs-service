package com.telecom.docs;

import com.telecom.config.SecurityConfig;
import com.telecom.controller.CustomerController;
import com.telecom.dto.CustomerRequest;
import com.telecom.dto.CustomerResponse;
import com.telecom.dto.PageResponse;
import com.telecom.dto.StatusChangeRequest;
import com.telecom.entity.CustomerStatus;
import com.telecom.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.telecom.example", uriPort = 443)
@Import({SecurityConfig.class, RestDocsConfiguration.class})
class CustomerDocumentationTest {

    private static final String CREATE_BODY = """
            {"name":"Arun Kumar","email":"arun@example.com","msisdn":"9876543210","planCode":"UNL-599"}""";

    private static final HeaderDescriptor AUTH = headerWithName(HttpHeaders.AUTHORIZATION)
            .description("HTTP Basic credentials of a user with role `AGENT` or `ADMIN`");

    private final ConstrainedFields customerFields = new ConstrainedFields(CustomerRequest.class);
    private final ConstrainedFields statusFields = new ConstrainedFields(StatusChangeRequest.class);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CustomerService service;

    @Test
    void createCustomer() throws Exception {
        when(service.create(any())).thenReturn(ApiDocs.CUSTOMER);

        mvc.perform(post("/api/v1/customers").with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
           .andExpect(status().isCreated())
           .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/v1/customers/42")))
           .andDo(document("customer-create",
                   requestHeaders(AUTH),
                   requestFields(
                           customerFields.withPath("name").description("Full name"),
                           customerFields.withPath("email").description("Email address, unique per customer"),
                           customerFields.withPath("msisdn").description("10-digit mobile number, unique"),
                           customerFields.withPath("planCode").description("Code of an active plan (see <<plans>>)")),
                   responseHeaders(headerWithName(HttpHeaders.LOCATION).description("URL of the new customer")),
                   responseFields(ApiDocs.customer())));
    }

    @Test
    void getCustomer() throws Exception {
        when(service.get(42L)).thenReturn(ApiDocs.CUSTOMER);

        mvc.perform(get("/api/v1/customers/{id}", 42L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD)))
           .andExpect(status().isOk())
           .andDo(document("customer-get",
                   requestHeaders(AUTH),
                   pathParameters(parameterWithName("id").description("Customer id")),
                   responseFields(ApiDocs.customer())));
    }

    @Test
    void listCustomers() throws Exception {
        when(service.list(eq(CustomerStatus.ACTIVE), any()))
                .thenReturn(new PageResponse<>(List.of(ApiDocs.CUSTOMER), 0, 20, 1, 1));

        mvc.perform(get("/api/v1/customers").with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD))
                        .param("status", "ACTIVE").param("page", "0").param("size", "20").param("sort", "name,asc"))
           .andExpect(status().isOk())
           .andDo(document("customers-list",
                   requestHeaders(AUTH),
                   queryParameters(
                           parameterWithName("status").optional().description("Filter: `ACTIVE`, `SUSPENDED` or `TERMINATED`"),
                           parameterWithName("page").optional().description("Zero-based page number (default 0)"),
                           parameterWithName("size").optional().description("Page size (default 20, max 100)"),
                           parameterWithName("sort").optional().description("`property,asc|desc` (default `id,asc`)")),
                   responseFields(ApiDocs.page()).andWithPrefix("content[].", ApiDocs.customer())));
    }

    @Test
    void updateCustomer() throws Exception {
        CustomerResponse updated = new CustomerResponse(42L, "Arun K", "arun.k@example.com", "9876543210",
                CustomerStatus.ACTIVE, ApiDocs.CUSTOMER.plan(), ApiDocs.CREATED, ApiDocs.UPDATED);
        when(service.update(eq(42L), any())).thenReturn(updated);

        mvc.perform(put("/api/v1/customers/{id}", 42L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Arun K","email":"arun.k@example.com","msisdn":"9876543210","planCode":"UNL-599"}"""))
           .andExpect(status().isOk())
           .andDo(document("customer-update",
                   requestHeaders(AUTH),
                   pathParameters(parameterWithName("id").description("Customer id")),
                   requestFields(
                           customerFields.withPath("name").description("Full name"),
                           customerFields.withPath("email").description("Email address"),
                           customerFields.withPath("msisdn").description("Mobile number"),
                           customerFields.withPath("planCode").description("Code of an active plan")),
                   responseFields(ApiDocs.customer())));
    }

    @Test
    void changeStatus() throws Exception {
        CustomerResponse suspended = new CustomerResponse(42L, "Arun Kumar", "arun@example.com", "9876543210",
                CustomerStatus.SUSPENDED, ApiDocs.CUSTOMER.plan(), ApiDocs.CREATED, ApiDocs.UPDATED);
        when(service.changeStatus(eq(42L), any())).thenReturn(suspended);

        mvc.perform(patch("/api/v1/customers/{id}/status", 42L).with(httpBasic(ApiDocs.AGENT, ApiDocs.AGENT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUSPENDED\",\"reason\":\"Non-payment\"}"))
           .andExpect(status().isOk())
           .andDo(document("customer-status-change",
                   requestHeaders(AUTH),
                   pathParameters(parameterWithName("id").description("Customer id")),
                   requestFields(
                           statusFields.withPath("status").description("Target status. `TERMINATED` is final"),
                           statusFields.withPath("reason").optional().description("Free-text reason for the audit log")),
                   responseFields(ApiDocs.customer())));
    }

    @Test
    void deleteCustomer() throws Exception {
        mvc.perform(delete("/api/v1/customers/{id}", 42L).with(httpBasic(ApiDocs.ADMIN, ApiDocs.ADMIN_PASSWORD)))
           .andExpect(status().isNoContent())
           .andDo(document("customer-delete",
                   requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("HTTP Basic credentials of an `ADMIN`")),
                   pathParameters(parameterWithName("id").description("Customer id"))));
        verify(service).delete(42L);
    }
}
