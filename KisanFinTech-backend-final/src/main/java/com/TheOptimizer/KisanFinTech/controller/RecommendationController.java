package com.TheOptimizer.KisanFinTech.controller;

import com.TheOptimizer.KisanFinTech.dto.RecommendationRequest;
import com.TheOptimizer.KisanFinTech.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService
    ) {

        this.recommendationService =
                recommendationService;
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>>
    getRecommendation(
            @Valid
            @RequestBody
            RecommendationRequest request
    ) {

        return ResponseEntity.ok(
                recommendationService
                        .getRecommendation(
                                request
                        )
        );
    }
}