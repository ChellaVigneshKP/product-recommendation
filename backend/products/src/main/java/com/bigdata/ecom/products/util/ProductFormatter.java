package com.bigdata.ecom.products.util;

import com.bigdata.ecom.products.model.FormattedProduct;
import com.bigdata.ecom.products.model.Product;
import com.bigdata.ecom.products.model.ViewedProductEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProductFormatter {
    private static final Logger logger = LoggerFactory.getLogger(ProductFormatter.class);

    private ProductFormatter() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static FormattedProduct formatProduct(Product product) {
        FormattedProduct formattedProduct = new FormattedProduct();
        formattedProduct.setId(product.getId());
        formattedProduct.setName(product.getName());
        formattedProduct.setDescription(product.getDescription());
        formattedProduct.setPrice(product.getDiscounted_price());
        formattedProduct.setCuttedPrice(product.getPrice());
        formattedProduct.setCategory(getFormattedCategory(product.getCategory(), product.getId()));
        formattedProduct.setStock(generateRandomStock());
        formattedProduct.setWarranty(getWarranty());
        formattedProduct.setRatings(product.getRatings());
        formattedProduct.setNumOfReviews(getNumOfReviews());
        formattedProduct.setReviews(getReviews());
        formattedProduct.setCreatedAt(String.valueOf(product.getCreatedAt()));
        formattedProduct.setBrand(formatBrand(product));
        formattedProduct.setImages(formatImages(product.getImageUrl()));
        formattedProduct.setSpecifications(getSpecifications(product.getProductSpecifications(), product.getId()));
        formattedProduct.setHighlights(getHighlights());
        formattedProduct.setVersion(0); // Placeholder for version (__v)
        return formattedProduct;
    }

    public static ViewedProductEvent formatViewedProductEvent(String userId, Product product, long timestamp, String eventType) {
        return new ViewedProductEvent(userId,
                product.getId(),
                product.getName(),
                getFormattedCategories(product.getCategory(), product.getId()),
                product.getBrand(),
                product.getPrice(),
                product.getDiscounted_price(),
                timestamp,
                product.getImageUrl(),
                "India",
                generateRandomStock(),
                product.getRatings(),
                eventType
        );
    }

    private static int generateRandomStock() {
        return (int) (Math.random() * 100) + 1;
    }

    private static int getWarranty() {
        return ThreadLocalRandom.current().nextInt(6, 37);
    }

    private static int getNumOfReviews() {
        return ThreadLocalRandom.current().nextInt(100, 4001);
    }

    private static List<Map<String, Object>> getReviews() {
        return new ArrayList<>(); // Placeholder, replace with actual data
    }

    private static Map<String, Object> formatBrand(Product product) {
        Map<String, Object> brandMap = new HashMap<>();
        brandMap.put("name", product.getBrand());
        Map<String, Object> brandLogo = new HashMap<>();
        brandLogo.put("public_id", "brands/mknipnyulamigp8w8csm"); // Replace with actual public ID
        brandLogo.put("url", "https://res.cloudinary.com/dvxwh8aqq/image/upload/v1686307212/brands/mknipnyulamigp8w8csm.png"); // Replace with actual logo URL
        brandMap.put("logo", brandLogo);
        return brandMap;
    }

    private static String getFormattedCategory(String productCategoryTree, String productId) {
        if (productCategoryTree == null || productCategoryTree.isEmpty()) {
            logger.info("Category tree is empty or null for product: {}", productId);
            return null;
        }
        productCategoryTree = productCategoryTree.replace("[", "").replace("]", "").trim();
        String[] categories = Arrays.stream(productCategoryTree.split("\\s*>\\s*>"))
                .map(String::trim) // Trim each category
                .filter(cat -> !cat.isEmpty()) // Remove empty categories
                .toArray(String[]::new);
        return (categories.length >= 2) ? categories[1] : categories[0];
    }

    private static List<String> getFormattedCategories(String productCategoryTree, String productId) {
        if (productCategoryTree == null || productCategoryTree.isEmpty()) {
            logger.info("Category tree is empty or null for product: {}", productId);
            return List.of();
        }
        productCategoryTree = productCategoryTree.replace("[", "").replace("]", "").trim();
        return Arrays.stream(productCategoryTree.split("\\s*>\\s*>"))
                .map(String::trim) // Trim each category
                .filter(cat -> !cat.isEmpty()) // Remove empty categories
                .toList(); // Collect as an immutable list
    }

    private static List<Map<String, Object>> formatImages(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return List.of(); // Return an empty map if no image URL is present
        }
        String[] imageUrls = imageUrl.replace("[", "").replace("]", "").split(", ");
        if (imageUrls.length > 0) {
            String trimmedUrl = imageUrls[0].trim(); // Pick the first image URL
            String publicId = "products/" + trimmedUrl.substring(trimmedUrl.lastIndexOf('/') + 1, trimmedUrl.lastIndexOf('.'));
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("url", trimmedUrl);
            imageMap.put("public_id", publicId);
            return List.of(imageMap); // Return a list containing only the first image
        }
        return List.of();
    }

    private static List<Map<String, Object>> getSpecifications(String productSpecifications, String productId) {
        if (productSpecifications == null || productSpecifications.isEmpty()) {
            return List.of(); // Return an empty list if no specifications are present
        }

        List<Map<String, Object>> specifications = new ArrayList<>();
        String uniqueIdBase = productId.substring(0, Math.min(productId.length(), 10)); // Derive base from product ID
        int idCounter = 0;

        try {
            // Step 1: Try JSON-like parsing
            if (productSpecifications.contains("product_specification")) {
                try {
                    String cleanJson = productSpecifications
                            .replace("'", "\"") // Replace single quotes with double quotes
                            .replace("\\u0027", "'") // Handle escaped single quotes
                            .replace("\\\"", "\""); // Handle escaped double quotes if any

                    Map<String, List<Map<String, String>>> specMap = new ObjectMapper().readValue(cleanJson, new TypeReference<>() {
                    });
                    List<Map<String, String>> productSpecificationList = specMap.get("product_specification");

                    for (Map<String, String> entry : productSpecificationList) {
                        String key = entry.get("key");
                        String value = entry.get("value");
                        if (key != null && value != null) {
                            Map<String, Object> specMapEntry = new HashMap<>();
                            specMapEntry.put("title", key);
                            specMapEntry.put("description", value);
                            specMapEntry.put("_id", uniqueIdBase + idCounter++);
                            specifications.add(specMapEntry);
                        }
                    }
                    return specifications; // Return if successful
                } catch (Exception e) {
                    logger.debug("JSON-like parsing failed for productId {}: {}", productId, e.getMessage());
                }
            }

            // Step 2: Try text-based parsing
            try {
                String trimmedSpecifications = productSpecifications.replace("{product_specification:[", "")
                        .replace("]}", ""); // Remove wrapping parts
                String[] specEntries = trimmedSpecifications.split("},"); // Split by each specification entry

                for (String entry : specEntries) {
                    entry = entry.replace("{", "").replace("}", "").trim(); // Clean up braces
                    String[] keyValue = entry.split(", "); // Split key-value pairs

                    String key = null;
                    String value = null;

                    // Extract key and value
                    for (String kv : keyValue) {
                        if (kv.startsWith("key:")) {
                            key = kv.replace("key:", "").trim();
                        } else if (kv.startsWith("value:")) {
                            value = kv.replace("value:", "").trim();
                        }
                    }

                    if (key != null && value != null) {
                        Map<String, Object> specMap = new HashMap<>();
                        specMap.put("title", key);
                        specMap.put("description", value);
                        specMap.put("_id", uniqueIdBase + idCounter++);
                        specifications.add(specMap);
                    }
                }
                return specifications; // Return if successful
            } catch (Exception e) {
                logger.error("Text-based parsing failed for productId {}: {}", productId, e.getMessage());
            }

            // Step 3: Custom fallback or log error
            logger.error("Failed to parse product specifications for productId {}: {}", productId, productSpecifications);
        } catch (Exception e) {
            logger.error("Unexpected error parsing specifications for productId {}: {}", productId, e.getMessage());
        }

        return specifications; // Return empty list if all parsing fails
    }


    private static List<String> getHighlights() {
        return List.of("Example highlight 1", "Example highlight 2"); // Placeholder, replace with actual highlights
    }
}