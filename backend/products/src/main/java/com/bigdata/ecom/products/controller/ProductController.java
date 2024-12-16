package com.bigdata.ecom.products.controller;

import com.bigdata.ecom.products.model.Product;
import com.bigdata.ecom.products.service.ProductService;
import com.bigdata.ecom.products.util.JwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final JwtUtil jwtUtil;
    private static final String VIEW = "view";
    private static final String SEARCH = "search";
    public ProductController(ProductService productService, JwtUtil jwtUtil) {
        this.productService = productService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(
            @PathVariable String id,
            @CookieValue(name = "token", required = false) String authToken) {
        String userId = null;
        try {
            if (authToken != null) {
                JWTClaimsSet claims = jwtUtil.validateToken(authToken);
                userId = claims.getSubject();
            }
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        Map<String, Object> response = productService.getProductById(id, userId, VIEW);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Product>> getRecommendations(@RequestParam String userId) {
        List<Product> recommendations = productService.getRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Product>> getLatestProducts() {
        List<Product> latestProducts = productService.getLatestProducts();
        return ResponseEntity.ok(latestProducts);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Map<String, Object> response = productService.getFormattedProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String category,
            @RequestParam(required = false, defaultValue = "0") double priceGte,
            @RequestParam(required = false, defaultValue = "200000") double priceLte,
            @RequestParam(required = false, defaultValue = "0") double ratingsGte,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        try {
            if (category != null && !category.isEmpty()) {
                category = URLDecoder.decode(category, StandardCharsets.UTF_8);
            }
            logger.debug("Decoded category: {}", category);
            logger.info("Received request with keyword: {}, category: {}, priceGte: {}, priceLte: {}, ratingsGte: {}, page: {}",
                    keyword, category, priceGte, priceLte, ratingsGte, page);
            Map<String, Object> response = productService.getFilteredProducts(keyword, category, priceGte, priceLte, ratingsGte, page);
            logger.info("Returning response with {} products", ((List<?>) response.get("products")).size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

}
