package com.telecom.controller;

import com.telecom.dto.PlanResponse;
import com.telecom.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlanResponse> list() {
        return service.listActive();
    }

    @GetMapping("/{code}")
    public PlanResponse get(@PathVariable String code) {
        return service.get(code);
    }
}
