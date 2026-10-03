package com.telecom.service;

import com.telecom.dto.CustomerRequest;
import com.telecom.dto.CustomerResponse;
import com.telecom.dto.StatusChangeRequest;
import com.telecom.entity.Customer;
import com.telecom.entity.CustomerStatus;
import com.telecom.entity.Plan;
import com.telecom.exception.DuplicateResourceException;
import com.telecom.exception.InvalidStateException;
import com.telecom.exception.ResourceNotFoundException;
import com.telecom.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final Plan PLAN = new Plan("UNL-599", "Unlimited 599", new BigDecimal("599.00"), 2, null);
    private static final CustomerRequest REQUEST =
            new CustomerRequest("Arun", "arun@example.com", "9876543210", "UNL-599");

    @Mock
    private CustomerRepository customers;

    @Mock
    private PlanService plans;

    @InjectMocks
    private CustomerService service;

    @Test
    void createsCustomerOnActivePlan() {
        when(plans.findActive("UNL-599")).thenReturn(PLAN);
        when(customers.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerResponse created = service.create(REQUEST);

        assertThat(created.plan().code()).isEqualTo("UNL-599");
        assertThat(created.status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void duplicateEmailIsRejected() {
        when(customers.existsByEmailIgnoreCase("arun@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(REQUEST)).isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
        verify(customers, never()).save(any());
    }

    @Test
    void duplicateMsisdnIsRejected() {
        when(customers.existsByMsisdn("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> service.create(REQUEST)).isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mobile");
    }

    @Test
    void unknownPlanIsNotFound() {
        when(plans.findActive("UNL-599")).thenThrow(new ResourceNotFoundException("No active plan with code UNL-599"));

        assertThatThrownBy(() -> service.create(REQUEST)).isInstanceOf(ResourceNotFoundException.class);
        verify(customers, never()).save(any());
    }

    @Test
    void suspendsActiveCustomer() {
        Customer customer = new Customer("Arun", "arun@example.com", "9876543210", PLAN);
        when(customers.findWithPlanById(1L)).thenReturn(Optional.of(customer));
        when(customers.saveAndFlush(customer)).thenReturn(customer);

        CustomerResponse response = service.changeStatus(1L, new StatusChangeRequest(CustomerStatus.SUSPENDED, null));

        assertThat(response.status()).isEqualTo(CustomerStatus.SUSPENDED);
    }

    @Test
    void terminatedCustomerCannotBeReactivated() {
        Customer customer = new Customer("Arun", "arun@example.com", "9876543210", PLAN);
        customer.changeStatus(CustomerStatus.TERMINATED);
        when(customers.findWithPlanById(1L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> service.changeStatus(1L, new StatusChangeRequest(CustomerStatus.ACTIVE, null)))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot change status from TERMINATED to ACTIVE");
        verify(customers, never()).saveAndFlush(any());
    }

    @Test
    void updateRejectsEmailOwnedByAnotherCustomer() {
        when(customers.findWithPlanById(1L)).thenReturn(Optional.of(new Customer("A", "a@example.com", "9000000001", PLAN)));
        when(customers.existsByEmailIgnoreCaseAndIdNot("arun@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, REQUEST)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void listUsesStatusFilterOnlyWhenGiven() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(customers.findAllBy(pageable)).thenReturn(Page.empty(pageable));
        when(customers.findByStatus(CustomerStatus.SUSPENDED, pageable)).thenReturn(Page.empty(pageable));

        service.list(null, pageable);
        service.list(CustomerStatus.SUSPENDED, pageable);

        verify(customers).findAllBy(pageable);
        verify(customers).findByStatus(CustomerStatus.SUSPENDED, pageable);
    }

    @Test
    void unknownCustomerIsNotFound() {
        when(customers.findWithPlanById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id 99");
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
