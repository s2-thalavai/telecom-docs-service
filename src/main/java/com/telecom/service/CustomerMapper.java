package com.telecom.service;

import com.telecom.dto.CustomerResponse;
import com.telecom.dto.CustomerV2Response;
import com.telecom.dto.PlanResponse;
import com.telecom.dto.PlanSummary;
import com.telecom.entity.Customer;
import com.telecom.entity.Plan;

final class CustomerMapper {

    private CustomerMapper() {
    }

    static CustomerResponse toV1(Customer c) {
        PlanSummary plan = c.getPlan() == null ? null : new PlanSummary(c.getPlan().getCode(), c.getPlan().getName());
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getMsisdn(), c.getStatus(), plan,
                c.getCreatedAt(), c.getUpdatedAt());
    }

    static CustomerV2Response toV2(Customer c) {
        Plan p = c.getPlan();
        return new CustomerV2Response(c.getId(), c.getName(), c.getStatus(),
                new CustomerV2Response.Contact(c.getEmail(), c.getMsisdn()),
                p == null ? null : new CustomerV2Response.Subscription(p.getCode(), p.getName(), p.getMonthlyPrice()),
                c.getCreatedAt());
    }

    static PlanResponse toResponse(Plan p) {
        return new PlanResponse(p.getCode(), p.getName(), p.getMonthlyPrice(), p.getDataAllowanceGb(), p.getVoiceMinutes());
    }
}
