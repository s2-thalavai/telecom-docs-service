package com.telecom.controller;

import com.telecom.dto.CustomerRequest;
import com.telecom.dto.CustomerResponse;
import com.telecom.dto.PageResponse;
import com.telecom.dto.StatusChangeRequest;
import com.telecom.entity.CustomerStatus;
import com.telecom.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = service.create(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri()).body(created);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public PageResponse<CustomerResponse> list(@RequestParam(required = false) CustomerStatus status,
                                               @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.list(status, pageable);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public CustomerResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return service.changeStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
