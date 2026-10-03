package com.telecom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;

/**
 * HTTP Basic, two roles. 401/403 bodies are ProblemDetails, so they can be documented like any other error.
 * Rules (documented in overview.adoc):
 *   GET /api/v1/plans/**        public
 *   DELETE /api/{v}/customers/** ADMIN
 *   /api/**                     AGENT or ADMIN
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        AuthenticationEntryPoint unauthorized = (request, response, ex) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"telecom\"");
            writeProblem(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "Unauthorized",
                    "Valid credentials are required for this resource");
        };
        AccessDeniedHandler forbidden = (request, response, ex) ->
                writeProblem(objectMapper, request, response, HttpStatus.FORBIDDEN, "Forbidden",
                        "Your role is not allowed to perform this operation");

        return http
                .csrf(csrf -> csrf.disable())                                // stateless API, no cookies
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/docs/**", "/actuator/health/**", "/error",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/plans/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/*/customers/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("AGENT", "ADMIN")
                        .anyRequest().denyAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorized))
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder,
                                    @Value("${telecom.security.agent-password:agent-pass}") String agentPassword,
                                    @Value("${telecom.security.admin-password:admin-pass}") String adminPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername("agent").password(encoder.encode(agentPassword)).roles("AGENT").build(),
                User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN").build());
    }

    private static void writeProblem(ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response,
                                     HttpStatus status, String title, String detail) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), pd);
    }
}
