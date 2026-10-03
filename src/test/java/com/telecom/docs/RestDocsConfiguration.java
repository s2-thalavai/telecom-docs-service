package com.telecom.docs;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

/** One place for how every example looks: pretty JSON, masked credentials, no framework header noise. */
@TestConfiguration(proxyBeanMethods = false)
public class RestDocsConfiguration {

    @Bean
    RestDocsMockMvcConfigurationCustomizer restDocsCustomizer() {
        return configurer -> configurer.operationPreprocessors()
                .withRequestDefaults(prettyPrint(), new MaskAuthorizationHeader())
                .withResponseDefaults(prettyPrint(), modifyHeaders()
                        .remove("X-Content-Type-Options").remove("X-XSS-Protection").remove("X-Frame-Options")
                        .remove("Cache-Control").remove("Pragma").remove("Expires").remove("Vary"));
    }
}
