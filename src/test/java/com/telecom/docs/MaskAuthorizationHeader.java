package com.telecom.docs;

import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Replaces real credentials in documented requests (only when an Authorization header is present).
 * The Basic placeholder must stay valid Base64: the curl/HTTPie snippets decode it into "-u user:password",
 * so they show {@code -u '<username>:<password>'}.
 */
public class MaskAuthorizationHeader implements OperationPreprocessor {

    static final String BASIC_PLACEHOLDER = "Basic " + Base64.getEncoder()
            .encodeToString("<username>:<password>".getBytes(StandardCharsets.UTF_8));
    static final String BEARER_PLACEHOLDER = "Bearer <access-token>";

    private final OperationRequestFactory factory = new OperationRequestFactory();

    @Override
    public OperationRequest preprocess(OperationRequest request) {
        String value = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null) {
            return request;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());
        headers.set(HttpHeaders.AUTHORIZATION, value.startsWith("Basic ") ? BASIC_PLACEHOLDER : BEARER_PLACEHOLDER);
        return factory.createFrom(request, headers);
    }

    @Override
    public OperationResponse preprocess(OperationResponse response) {
        return response;
    }
}
