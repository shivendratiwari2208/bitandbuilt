package com.TheOptimizer.KisanFinTech.controller;

import com.TheOptimizer.KisanFinTech.dto.RecommendationRequest;
import com.TheOptimizer.KisanFinTech.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;
    public RecommendationController(RecommendationService service){this.service=service;}
    @PostMapping
    public Map<String,Object> recommend(@Valid @RequestBody RecommendationRequest request){return service.getRecommendation(request);}
}
