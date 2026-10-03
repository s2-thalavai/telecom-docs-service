package com.telecom.service;

import com.telecom.dto.PlanResponse;
import com.telecom.entity.Plan;
import com.telecom.exception.ResourceNotFoundException;
import com.telecom.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository plans;

    public PlanService(PlanRepository plans) {
        this.plans = plans;
    }

    public List<PlanResponse> listActive() {
        return plans.findByActiveTrueOrderByMonthlyPriceAsc().stream().map(CustomerMapper::toResponse).toList();
    }

    public PlanResponse get(String code) {
        return CustomerMapper.toResponse(findActive(code));
    }

    Plan findActive(String code) {
        return plans.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("No active plan with code " + code));
    }
}
