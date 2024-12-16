package com.bigdata.ecom.products.controller;

import com.bigdata.ecom.products.model.FormattedProduct;
import com.bigdata.ecom.products.model.Product;
import com.bigdata.ecom.products.service.RedisService;
import com.bigdata.ecom.products.util.JwtUtil;
import com.bigdata.ecom.products.util.ProductFormatter;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products/recommendations")
public class RecommendationController {
    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    public RecommendationController(RedisService redisService, JwtUtil jwtUtil) {
        this.redisService = redisService;
        this.jwtUtil = jwtUtil;
    }
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRecommendationsByUserId(@CookieValue(name = "token", required = false) String authToken) {
        String userId = null;
        List<FormattedProduct> filteredProducts = List.of();
        Map<String, Object> response = new HashMap<>();
        try {
            if (authToken != null) {
                JWTClaimsSet claims = jwtUtil.validateToken(authToken);
                userId = claims.getSubject();
                List<Product> recommendations = redisService.getRecommendationsByUserId(userId);
                filteredProducts = recommendations.stream()
                        .map(ProductFormatter::formatProduct)
                        .toList();
                response.put("success", true);
            }else {
                response.put("success", false);
            }
        } catch (Exception e) {
            response.put("success", false);
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        response.put("products", filteredProducts);
        return ResponseEntity.ok(response);
    }
}
