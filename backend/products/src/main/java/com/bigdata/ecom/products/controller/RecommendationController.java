package com.bigdata.ecom.products.controller;

import com.bigdata.ecom.products.model.FormattedProduct;
import com.bigdata.ecom.products.model.Product;
import com.bigdata.ecom.products.service.RedisService;
import com.bigdata.ecom.products.util.ProductFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products/recommendations")
public class RecommendationController {
    private final RedisService redisService;

    public RecommendationController(RedisService redisService) {
        this.redisService = redisService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getRecommendationsByUserId(@PathVariable String userId) {
        List<Product> recommendations = redisService.getRecommendationsByUserId(userId);
        List<FormattedProduct> filteredProducts = recommendations.stream()
                .map(ProductFormatter::formatProduct)
                .toList();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("products", filteredProducts);
        return ResponseEntity.ok(response);
    }
}
