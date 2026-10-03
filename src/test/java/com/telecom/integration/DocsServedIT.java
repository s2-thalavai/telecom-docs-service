package com.telecom.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs after `prepare-package`: the HTML rendered by Asciidoctor was copied into target/classes/static/docs,
 * so the application must serve it publicly, with every documented section present.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocsServedIT {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void documentationIsServedWithoutCredentials() {
        ResponseEntity<String> page = rest.getForEntity("/docs/index.html", String.class);

        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody())
                .contains("Telecom Customer API")
                .contains("id=\"customer-create\"")
                .contains("id=\"v2-customer-get\"")
                .contains("id=\"errors-validation\"")
                .contains("api.telecom.example")                       // examples use the public host
                .contains("&lt;username&gt;:&lt;password&gt;")         // curl example shows masked credentials
                .doesNotContain("agent-pass")
                .doesNotContain("YWdlbnQ6YWdlbnQtcGFzcw==");            // Base64 of agent:agent-pass
    }

    @Test
    void rootRedirectsToTheDocs() {
        ResponseEntity<String> root = rest.getForEntity("/", String.class);

        assertThat(root.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(root.getBody()).contains("/docs/index.html");
    }
}
