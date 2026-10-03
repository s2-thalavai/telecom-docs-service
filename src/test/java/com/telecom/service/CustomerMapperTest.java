package com.telecom.service;

import com.telecom.dto.CustomerResponse;
import com.telecom.dto.CustomerV2Response;
import com.telecom.dto.PlanResponse;
import com.telecom.entity.Customer;
import com.telecom.entity.CustomerStatus;
import com.telecom.entity.Plan;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final Plan plan = new Plan("UNL-599", "Unlimited 599", new BigDecimal("599.00"), 2, null);

    @Test
    void v1FlattensPlanToSummary() {
        Customer customer = customer(plan);

        CustomerResponse v1 = CustomerMapper.toV1(customer);

        assertThat(v1.id()).isEqualTo(7L);
        assertThat(v1.plan().code()).isEqualTo("UNL-599");
        assertThat(v1.status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void v2GroupsContactAndSubscription() {
        CustomerV2Response v2 = CustomerMapper.toV2(customer(plan));

        assertThat(v2.contact().email()).isEqualTo("a@example.com");
        assertThat(v2.contact().msisdn()).isEqualTo("9876543210");
        assertThat(v2.subscription().monthlyPrice()).isEqualByComparingTo("599");
    }

    @Test
    void customerWithoutPlanHasNoPlanInEitherVersion() {
        Customer customer = customer(null);

        assertThat(CustomerMapper.toV1(customer).plan()).isNull();
        assertThat(CustomerMapper.toV2(customer).subscription()).isNull();
    }

    @Test
    void planResponseKeepsUnlimitedMinutesAsNull() {
        PlanResponse response = CustomerMapper.toResponse(plan);

        assertThat(response.voiceMinutes()).isNull();
        assertThat(response.dataAllowanceGb()).isEqualTo(2);
    }

    private static Customer customer(Plan plan) {
        Customer customer = new Customer("Arun", "a@example.com", "9876543210", plan);
        ReflectionTestUtils.setField(customer, "id", 7L);
        return customer;
    }
}
