package com.telecom.repository;

import com.telecom.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByActiveTrueOrderByMonthlyPriceAsc();

    Optional<Plan> findByCodeAndActiveTrue(String code);
}
