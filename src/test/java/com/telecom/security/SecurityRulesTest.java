package com.telecom.security;

import com.telecom.config.SecurityConfig;
import com.telecom.service.CustomerService;
import com.telecom.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The access rules documented in overview.adoc, verified for every controller. */
@WebMvcTest
@Import(SecurityConfig.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CustomerService customers;

    @MockitoBean
    private PlanService plans;

    @Test
    void plansArePublic() throws Exception {
        when(plans.listActive()).thenReturn(List.of());
        mvc.perform(get("/api/v1/plans")).andExpect(status().isOk());
    }

    @Test
    void customersNeedCredentials() throws Exception {
        mvc.perform(get("/api/v1/customers/1"))
           .andExpect(status().isUnauthorized())
           .andExpect(header().string("WWW-Authenticate", "Basic realm=\"telecom\""))
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
           .andExpect(jsonPath("$.instance").value("/api/v1/customers/1"));
        mvc.perform(get("/api/v2/customers/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/customers/1").with(httpBasic("agent", "wrong")))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void agentCanReadAndWriteButNotDelete() throws Exception {
        mvc.perform(get("/api/v1/customers").with(httpBasic("agent", "agent-pass"))).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/customers/1").with(httpBasic("agent", "agent-pass")))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void adminCanDelete() throws Exception {
        mvc.perform(delete("/api/v1/customers/1").with(httpBasic("admin", "admin-pass")))
           .andExpect(status().isNoContent());
    }

    @Test
    void writingPlansIsNotPublic() throws Exception {
        mvc.perform(post("/api/v1/plans").contentType(MediaType.APPLICATION_JSON).content("{}"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownNonApiPathsAreDenied() throws Exception {
        mvc.perform(get("/internal/anything").with(httpBasic("admin", "admin-pass")))
           .andExpect(status().isForbidden());
    }
}
