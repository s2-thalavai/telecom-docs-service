package com.telecom.repository;

import com.telecom.entity.Customer;
import com.telecom.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @EntityGraph(attributePaths = "plan")
    Optional<Customer> findWithPlanById(Long id);

    @EntityGraph(attributePaths = "plan")
    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "plan")
    Page<Customer> findAllBy(Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMsisdn(String msisdn);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByMsisdnAndIdNot(String msisdn, Long id);
}
