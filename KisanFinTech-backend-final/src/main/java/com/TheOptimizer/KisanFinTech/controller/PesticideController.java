package com.TheOptimizer.KisanFinTech.controller;

import com.TheOptimizer.KisanFinTech.dto.PesticideRequest;
import com.TheOptimizer.KisanFinTech.service.PesticideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pesticide")
public class PesticideController {

    private final PesticideService pesticideService;

    public PesticideController(
            PesticideService pesticideService
    ) {
        this.pesticideService =
                pesticideService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>>
    recommend(
            @Valid
            @RequestBody
            PesticideRequest request
    ) {

        return ResponseEntity.ok(
                pesticideService
                        .getRecommendation(
                                request
                        )
        );
    }
}