package com.telecom.config;

import com.telecom.entity.Customer;
import com.telecom.entity.Plan;
import com.telecom.repository.CustomerRepository;
import com.telecom.repository.PlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Sample data for local runs. In prod, Flyway V2 seeds the plans. */
@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final PlanRepository plans;
    private final CustomerRepository customers;

    public DataSeeder(PlanRepository plans, CustomerRepository customers) {
        this.plans = plans;
        this.customers = customers;
    }

    @Override
    public void run(String... args) {
        if (plans.count() > 0) {
            return;
        }
        Plan unlimited = plans.save(new Plan("UNL-599", "Unlimited 599", new BigDecimal("599.00"), 2, null));
        plans.save(new Plan("DATA-299", "Data 299", new BigDecimal("299.00"), 1, 300));
        customers.save(new Customer("Arun Kumar", "arun@example.com", "9876543210", unlimited));
    }
}
