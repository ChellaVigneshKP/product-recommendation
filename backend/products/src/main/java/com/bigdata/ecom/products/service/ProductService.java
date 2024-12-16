package com.bigdata.ecom.products.service;

import com.bigdata.ecom.products.exception.ProductNotFoundException;
import com.bigdata.ecom.products.model.FormattedProduct;
import com.bigdata.ecom.products.model.Product;
import com.bigdata.ecom.products.model.ViewedProductEvent;
import com.bigdata.ecom.products.util.ProductFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final BigQueryService bigQueryService;
    private final InteractionProducer interactionProducer;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String SUCCESS = "success";

    public ProductService(BigQueryService bigQueryService, InteractionProducer interactionProducer) {
        this.bigQueryService = bigQueryService;
        this.interactionProducer = interactionProducer;
    }

    public Map<String, Object> getProductById(String id, String userId, String eventType) {
        Product product = bigQueryService.fetchProductById(id);
        logger.info("Product fetched: {}", product);
        if (product == null) {
            throw new ProductNotFoundException("Product not found for ID: " + id);
        }
        if (userId != null) {
            ViewedProductEvent event = ProductFormatter.formatViewedProductEvent(userId, product, System.currentTimeMillis(), eventType);
            interactionProducer.sendViewedProductEvent(event,userId);
        }

        FormattedProduct formattedProduct = ProductFormatter.formatProduct(product);
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("product", formattedProduct);
        return response;
    }

    public List<Product> getRecommendations(String userId) {
        // Fetch recommendations (query recommendation engine or BigQuery)
        return bigQueryService.fetchRecommendations(userId);
    }

    public List<Product> getLatestProducts() {
        // Fetch latest products
        return bigQueryService.fetchLatestProducts();
    }

    public Map<String, Object> getFormattedProducts(int page, int size) {
        List<Product> products = bigQueryService.fetchAllProducts(page, size);
        List<FormattedProduct> formattedProducts = products.stream()
                .map(ProductFormatter::formatProduct)
                .toList();
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("products", formattedProducts);
        return response;
    }


    public Map<String, Object> getFilteredProducts(String keyword, String category, double priceGte, double priceLte, double ratingsGte, int page) {
        // Fetch products based on filters
        Map<String, Object> products = bigQueryService.getFilteredProducts(keyword, category, priceGte, priceLte, ratingsGte, page);
        List<Product> productsList = (List<Product>) products.get("products");
        List<FormattedProduct> filteredProducts = productsList.stream()
                .map(ProductFormatter::formatProduct)
                .toList();
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("products", filteredProducts);
        response.put("filteredProductsCount", products.get("filteredProductsCount"));
        response.put("resultPerPage", products.get("resultPerPage"));
        response.put("productsCount", products.get("productsCount"));

        return response;
    }
}

