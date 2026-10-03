package com.telecom.service;

import com.telecom.dto.CustomerRequest;
import com.telecom.dto.CustomerResponse;
import com.telecom.dto.CustomerV2Response;
import com.telecom.dto.PageResponse;
import com.telecom.dto.StatusChangeRequest;
import com.telecom.entity.Customer;
import com.telecom.entity.CustomerStatus;
import com.telecom.exception.DuplicateResourceException;
import com.telecom.exception.InvalidStateException;
import com.telecom.exception.ResourceNotFoundException;
import com.telecom.repository.CustomerRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customers;
    private final PlanService plans;

    public CustomerService(CustomerRepository customers, PlanService plans) {
        this.customers = customers;
        this.plans = plans;
    }

    public CustomerResponse get(Long id) {
        return CustomerMapper.toV1(find(id));
    }

    public CustomerV2Response getV2(Long id) {
        return CustomerMapper.toV2(find(id));
    }

    public PageResponse<CustomerResponse> list(CustomerStatus status, Pageable pageable) {
        return PageResponse.from((status == null ? customers.findAllBy(pageable) : customers.findByStatus(status, pageable))
                .map(CustomerMapper::toV1));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customers.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        if (customers.existsByMsisdn(request.msisdn())) {
            throw new DuplicateResourceException("Mobile number already registered: " + request.msisdn());
        }
        Customer saved = customers.save(new Customer(request.name(), request.email(), request.msisdn(),
                plans.findActive(request.planCode())));
        return CustomerMapper.toV1(saved);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = find(id);
        if (customers.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        if (customers.existsByMsisdnAndIdNot(request.msisdn(), id)) {
            throw new DuplicateResourceException("Mobile number already registered: " + request.msisdn());
        }
        customer.update(request.name(), request.email(), request.msisdn(), plans.findActive(request.planCode()));
        return CustomerMapper.toV1(customers.saveAndFlush(customer));
    }

    @Transactional
    public CustomerResponse changeStatus(Long id, StatusChangeRequest request) {
        Customer customer = find(id);
        if (!customer.getStatus().canChangeTo(request.status())) {
            throw new InvalidStateException("Cannot change status from %s to %s"
                    .formatted(customer.getStatus(), request.status()));
        }
        customer.changeStatus(request.status());
        return CustomerMapper.toV1(customers.saveAndFlush(customer));
    }

    @Transactional
    public void delete(Long id) {
        customers.delete(find(id));
    }

    private Customer find(Long id) {
        return customers.findWithPlanById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }
}
