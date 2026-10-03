package com.telecom.controller;

import com.telecom.dto.CustomerV2Response;
import com.telecom.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/customers")
public class CustomerV2Controller {

    private final CustomerService service;

    public CustomerV2Controller(CustomerService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public CustomerV2Response get(@PathVariable Long id) {
        return service.getV2(id);
    }
}
