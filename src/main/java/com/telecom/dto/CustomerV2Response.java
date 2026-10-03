package com.telecom.dto;

import com.telecom.entity.CustomerStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** v2 representation: contact details and subscription grouped. */
public record CustomerV2Response(Long id, String name, CustomerStatus status, Contact contact,
                                 Subscription subscription, Instant memberSince) {

    public record Contact(String email, String msisdn) {
    }

    public record Subscription(String planCode, String planName, BigDecimal monthlyPrice) {
    }
}
