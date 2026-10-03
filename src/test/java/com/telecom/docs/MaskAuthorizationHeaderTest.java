package com.telecom.docs;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MaskAuthorizationHeaderTest {

    private final MaskAuthorizationHeader preprocessor = new MaskAuthorizationHeader();

    @Test
    void basicCredentialsAreReplacedWithDecodablePlaceholder() {
        OperationRequest masked = preprocessor.preprocess(request("Basic " + base64("agent:agent-pass")));

        String header = masked.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assertThat(new String(Base64.getDecoder().decode(header.substring(6)), StandardCharsets.UTF_8))
                .isEqualTo("<username>:<password>");
    }

    @Test
    void bearerTokensAreReplaced() {
        OperationRequest masked = preprocessor.preprocess(request("Bearer eyJhbGciOi.secret.sig"));

        assertThat(masked.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer <access-token>");
    }

    @Test
    void requestsWithoutCredentialsAreUntouched() {
        OperationRequest request = request(null);

        assertThat(preprocessor.preprocess(request)).isSameAs(request);
    }

    @Test
    void otherHeadersAreKept() {
        OperationRequest masked = preprocessor.preprocess(request("Basic " + base64("a:b")));

        assertThat(masked.getHeaders().getFirst("X-Request-Id")).isEqualTo("abc-123");
    }

    private static OperationRequest request(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "abc-123");
        if (authorization != null) {
            headers.add(HttpHeaders.AUTHORIZATION, authorization);
        }
        return new OperationRequestFactory().create(URI.create("https://api.telecom.example/api/v1/customers/1"),
                HttpMethod.GET, new byte[0], headers, Collections.emptyList(), Collections.emptyList());
    }

    private static String base64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
}
