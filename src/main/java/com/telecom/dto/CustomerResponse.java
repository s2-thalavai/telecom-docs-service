package com.telecom.dto;

import com.telecom.entity.CustomerStatus;

import java.time.Instant;

public record CustomerResponse(Long id, String name, String email, String msisdn, CustomerStatus status,
                               PlanSummary plan, Instant createdAt, Instant updatedAt) {
}
