package com.telecom.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** PROD profile on MySQL 8.4: Flyway schema matches the JPA mappings. Skipped without Docker. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("prod")
@Testcontainers(disabledWithoutDocker = true)
class MySqlFlywayIT {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private Flyway flyway;

    @Test
    void migrationsAppliedAndPlansSeeded() {
        assertThat(Arrays.stream(flyway.info().applied()).map(m -> m.getVersion().getVersion()))
                .containsExactly("1", "2");
        assertThat(rest.getForObject("/api/v1/plans", JsonNode.class)).hasSize(2);
    }

    @Test
    void createAndReadCustomerOnMySql() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> created = rest.withBasicAuth("agent", "agent-pass").postForEntity("/api/v1/customers",
                new HttpEntity<>(Map.of("name", "MySQL User", "email", "mysql@example.com",
                        "msisdn", "9811111111", "planCode", "DATA-299"), headers), JsonNode.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode v2 = rest.withBasicAuth("agent", "agent-pass")
                .getForObject("/api/v2/customers/" + created.getBody().get("id").asLong(), JsonNode.class);
        assertThat(v2.get("subscription").get("planCode").asText()).isEqualTo("DATA-299");
    }
}
