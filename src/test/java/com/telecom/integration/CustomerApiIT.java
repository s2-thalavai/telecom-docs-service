package com.telecom.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The real application (H2, seeded plans, real security) over HTTP: confirms that what the documentation
 * tests describe with mocked services is also what the full stack does.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerApiIT {

    private static final AtomicInteger SEQ = new AtomicInteger(100);

    @Autowired
    private TestRestTemplate rest;

    private TestRestTemplate agent() {
        return rest.withBasicAuth("agent", "agent-pass");
    }

    private TestRestTemplate admin() {
        return rest.withBasicAuth("admin", "admin-pass");
    }

    private static Map<String, Object> newCustomer() {
        int n = SEQ.incrementAndGet();
        return Map.of("name", "IT Customer " + n, "email", "it" + n + "@example.com",
                "msisdn", "9%09d".formatted(n), "planCode", "UNL-599");
    }

    private static HttpEntity<Object> json(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void plansArePublicAndSortedByPrice() {
        ResponseEntity<JsonNode> response = rest.getForEntity("/api/v1/plans", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).get("code").asText()).isEqualTo("DATA-299");
        assertThat(response.getBody().get(1).has("voiceMinutes")).isFalse();     // unlimited -> omitted
    }

    @Test
    void customerLifecycle() {
        // create
        ResponseEntity<JsonNode> created = agent().postForEntity("/api/v1/customers", json(newCustomer()), JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String location = created.getHeaders().getLocation().getPath();
        long id = created.getBody().get("id").asLong();
        assertThat(location).isEqualTo("/api/v1/customers/" + id);
        assertThat(created.getBody().has("updatedAt")).isFalse();

        // read v1 and v2
        assertThat(agent().getForObject(location, JsonNode.class).get("plan").get("code").asText()).isEqualTo("UNL-599");
        JsonNode v2 = agent().getForObject("/api/v2/customers/" + id, JsonNode.class);
        assertThat(v2.get("contact").get("email").asText()).startsWith("it");
        assertThat(v2.get("subscription").get("monthlyPrice").decimalValue()).isEqualByComparingTo("599");

        // status transitions
        assertThat(patchStatus(id, "SUSPENDED").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchStatus(id, "TERMINATED").getBody().get("status").asText()).isEqualTo("TERMINATED");
        ResponseEntity<JsonNode> reactivate = patchStatus(id, "ACTIVE");
        assertThat(reactivate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(reactivate.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);

        // delete: agent forbidden, admin allowed
        assertThat(agent().exchange(location, HttpMethod.DELETE, null, JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(admin().exchange(location, HttpMethod.DELETE, null, Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(agent().getForEntity(location, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateChangesFieldsAndSetsUpdatedAt() {
        long id = agent().postForEntity("/api/v1/customers", json(newCustomer()), JsonNode.class).getBody().get("id").asLong();
        Map<String, Object> changed = newCustomer();

        ResponseEntity<JsonNode> updated = agent().exchange("/api/v1/customers/" + id, HttpMethod.PUT,
                json(Map.of("name", "Renamed", "email", changed.get("email"), "msisdn", changed.get("msisdn"),
                        "planCode", "DATA-299")), JsonNode.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().get("name").asText()).isEqualTo("Renamed");
        assertThat(updated.getBody().get("plan").get("code").asText()).isEqualTo("DATA-299");
        assertThat(updated.getBody().has("updatedAt")).isTrue();
    }

    @Test
    void errorsMatchTheDocumentedProblemFormat() {
        Map<String, Object> customer = newCustomer();
        agent().postForEntity("/api/v1/customers", json(customer), JsonNode.class);

        ResponseEntity<JsonNode> duplicate = agent().postForEntity("/api/v1/customers", json(customer), JsonNode.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicate.getBody().get("title").asText()).isEqualTo("Duplicate Resource");
        assertThat(duplicate.getBody().get("instance").asText()).isEqualTo("/api/v1/customers");

        ResponseEntity<JsonNode> invalid = agent().postForEntity("/api/v1/customers",
                json(Map.of("name", "", "email", "bad", "msisdn", "1", "planCode", "UNL-599")), JsonNode.class);
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalid.getBody().get("errors").has("email")).isTrue();
        assertThat(invalid.getBody().get("errors").has("msisdn")).isTrue();

        Map<String, Object> unknownPlan = newCustomer();
        ResponseEntity<JsonNode> notFound = agent().postForEntity("/api/v1/customers",
                json(Map.of("name", "X", "email", unknownPlan.get("email"), "msisdn", unknownPlan.get("msisdn"),
                        "planCode", "GOLD-999")), JsonNode.class);
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<JsonNode> unauthorized = rest.getForEntity("/api/v1/customers/1", JsonNode.class);
        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(unauthorized.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Basic");
    }

    @Test
    void listIsPagedAndFilterable() {
        long id = agent().postForEntity("/api/v1/customers", json(newCustomer()), JsonNode.class).getBody().get("id").asLong();
        patchStatus(id, "SUSPENDED");

        JsonNode page = agent().getForObject("/api/v1/customers?status=SUSPENDED&size=500", JsonNode.class);

        assertThat(page.get("size").asInt()).isEqualTo(100);                      // capped by max-page-size
        assertThat(page.get("content")).allSatisfy(c -> assertThat(c.get("status").asText()).isEqualTo("SUSPENDED"));
        assertThat(page.get("totalElements").asLong()).isGreaterThanOrEqualTo(1);
    }

    private ResponseEntity<JsonNode> patchStatus(long id, String status) {
        return agent().exchange("/api/v1/customers/" + id + "/status", HttpMethod.PATCH,
                json(Map.of("status", status)), JsonNode.class);
    }
}
