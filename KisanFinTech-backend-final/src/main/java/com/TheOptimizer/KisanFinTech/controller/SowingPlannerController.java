package com.TheOptimizer.KisanFinTech.controller;

import com.TheOptimizer.KisanFinTech.dto.SowingPlannerRequest;
import com.TheOptimizer.KisanFinTech.service.SowingPlannerService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sowing-planner")
@CrossOrigin(origins = "*")
public class SowingPlannerController {

    private final SowingPlannerService sowingPlannerService;

    public SowingPlannerController(
            SowingPlannerService sowingPlannerService
    ) {
        this.sowingPlannerService =
                sowingPlannerService;
    }

    @PostMapping("/plan")
    public ResponseEntity<Map<String, Object>> createPlan(
            @Valid @RequestBody SowingPlannerRequest request
    ) {

        return ResponseEntity.ok(
                sowingPlannerService.createPlan(
                        request
                )
        );
    }
}