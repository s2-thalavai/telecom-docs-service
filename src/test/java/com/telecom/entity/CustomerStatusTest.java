package com.telecom.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerStatusTest {

    @ParameterizedTest(name = "{0} -> {1} allowed={2}")
    @CsvSource({
            "ACTIVE,     SUSPENDED,  true",
            "ACTIVE,     TERMINATED, true",
            "ACTIVE,     ACTIVE,     false",
            "SUSPENDED,  ACTIVE,     true",
            "SUSPENDED,  TERMINATED, true",
            "SUSPENDED,  SUSPENDED,  false",
            "TERMINATED, ACTIVE,     false",
            "TERMINATED, SUSPENDED,  false",
            "TERMINATED, TERMINATED, false"
    })
    void transitions(CustomerStatus from, CustomerStatus to, boolean allowed) {
        assertThat(from.canChangeTo(to)).isEqualTo(allowed);
    }
}
