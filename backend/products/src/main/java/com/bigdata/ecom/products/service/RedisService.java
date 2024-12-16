package com.bigdata.ecom.products.service;

import com.bigdata.ecom.products.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Product> getRecommendationsByUserId(String userId) {
        String redisKeyPattern = String.format("user:%s:category:*", userId);

        Set<String> keys = redisTemplate.keys(redisKeyPattern);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        return keys.stream()
                .flatMap(key -> {
                    String recommendationsJson = redisTemplate.opsForValue().get(key);
                    if (recommendationsJson != null) {
                        try {
                            // Deserialize JSON to List<Product>
                            return objectMapper.readValue(recommendationsJson, new TypeReference<List<Product>>() {
                                    })
                                    .stream();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize recommendations from Redis for key: " + key, e);
                        }
                    }
                    return Stream.empty();
                })
                .toList();
    }
}