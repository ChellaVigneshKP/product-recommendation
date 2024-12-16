package com.bigdata.ecom.products.util;

import com.bigdata.ecom.products.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;

public class ProductMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Product mapJsonToProduct(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);

            Product product = new Product();
            product.setId(rootNode.get("uniq_id").asText());
            product.setName(rootNode.get("product_name").asText());
            product.setDescription(rootNode.get("description").asText());
            product.setPrice(rootNode.get("retail_price").asDouble());
            product.setDiscounted_price(rootNode.get("discounted_price").asDouble());
            product.setCategory(rootNode.get("product_category_tree").asText());
            product.setBrand(rootNode.get("brand").asText());

            // Extract the first image URL from the array of images
            String imageUrls = rootNode.get("image").asText();
            if (imageUrls != null && imageUrls.startsWith("[")) {
                // Remove the brackets and split by comma
                String[] images = imageUrls.replaceAll("[\\[\\]\"]", "").split(",");
                product.setImageUrl(images.length > 0 ? images[0].trim() : null);
            } else {
                product.setImageUrl(imageUrls);
            }

            product.setRatings(rootNode.get("overall_rating").asDouble());
            product.setProductSpecifications(rootNode.get("product_specifications").asText());

            // Convert crawl_timestamp to Google Cloud Timestamp (optional)
            String crawlTimestamp = rootNode.get("crawl_timestamp").asText();
            if (crawlTimestamp != null && !crawlTimestamp.isEmpty()) {
                product.setCreatedAt(Timestamp.parseTimestamp(crawlTimestamp));
            }

            return product;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JSON to Product: " + e.getMessage(), e);
        }
    }
}

