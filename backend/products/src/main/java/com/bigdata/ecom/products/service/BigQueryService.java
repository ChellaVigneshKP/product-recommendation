package com.bigdata.ecom.products.service;

import com.bigdata.ecom.products.model.Product;
import com.google.cloud.bigquery.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BigQueryService {

    private final BigQuery bigQuery;
    private static final Logger logger = LoggerFactory.getLogger(BigQueryService.class);

    public BigQueryService(BigQuery bigQuery) {
        this.bigQuery = bigQuery;
    }

    public Product fetchProductById(String id) {
        String query = "SELECT * FROM `virtualization-and-cloud.amazon_bigdata.flipkart-data` WHERE uniq_id = @id";
        logger.debug("Executing query: {}", query);
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
                .addNamedParameter("id", QueryParameterValue.string(id))
                .build();

        try {
            TableResult result = bigQuery.query(queryConfig);
            if (result.getTotalRows() > 0) {
                FieldValueList row = result.iterateAll().iterator().next(); // Fetch the first row
                return mapRowToProduct(row);
            } else {
                return null; // No matching product found
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Query execution interrupted", e);
        }
    }

    public List<Product> fetchRecommendations(String userId) {
        // Query recommendation table in BigQuery
        String query = String.format("SELECT * FROM recommendations WHERE user_id = '%s'", userId);
        return executeQueryForList(query, Product.class);
    }

    public List<Product> fetchLatestProducts() {
        // Query latest products based on created_at timestamp
        String query = "SELECT * FROM products ORDER BY created_at DESC LIMIT 10";
        return executeQueryForList(query, Product.class);
    }

    private <T> T executeQueryForSingleResult(String query, Class<T> type) {
        // Execute query and map results
        // Add implementation for BigQuery query execution
        return null;
    }

    private <T> List<T> executeQueryForList(String query, Class<T> type) {
        // Execute query and map results
        // Add implementation for BigQuery query execution
        return new ArrayList<>();
    }

    public List<Product> fetchAllProducts(int page, int size) {
        int offset = page * size;
        String query = String.format(
                "SELECT * FROM `virtualization-and-cloud.amazon_bigdata.flipkart-data` LIMIT %d OFFSET %d",
                size, offset
        );
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();

        TableResult result = executeQuery(queryConfig);
        List<Product> products = new ArrayList<>();
        for (FieldValueList row : result.iterateAll()) {
            products.add(mapRowToProduct(row)); // Map each row to Product object
        }
        return products;
    }

    private TableResult executeQuery(QueryJobConfiguration queryConfig) {
        try {
            Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).build());
            queryJob = queryJob.waitFor();
            if (queryJob == null) {
                throw new RuntimeException("Job no longer exists.");
            } else if (queryJob.getStatus().getError() != null) {
                throw new RuntimeException(queryJob.getStatus().getError().toString());
            }
            return queryJob.getQueryResults();
        } catch (InterruptedException e) {
            throw new RuntimeException("Query interrupted: " + e.getMessage());
        }
    }

    private Product mapRowToProduct(FieldValueList row) {
        Product product = new Product();
        product.setId(row.get("uniq_id").getStringValue()); // Use 'uniq_id' instead of 'id'
        product.setName(row.get("product_name").getStringValue());
        product.setPrice(Double.parseDouble(row.get("retail_price").getStringValue()));
        product.setDescription(row.get("description").getStringValue());
        product.setCategory(row.get("product_category_tree").getStringValue());
        product.setImageUrl(row.get("image").getStringValue()); // Adjust this if you have a list
        product.setBrand(row.get("brand").getStringValue());
        product.setProductSpecifications(row.get("product_specifications").getStringValue());
        product.setDiscounted_price(Double.parseDouble(row.get("discounted_price").getStringValue()));
        product.setRatings(Double.parseDouble(row.get("overall_rating").getStringValue()));
        return product;
    }

    public Map<String, Object> getFilteredProducts(String keyword, String category, double priceGte, double priceLte, double ratingsGte, int page) {
        int pageSize = 24; // Number of products per page
        int offset = (page - 1) * pageSize; // Calculate offset for pagination

        // Query for total products count
        String totalProductsQuery = "SELECT COUNT(*) AS count FROM `virtualization-and-cloud.amazon_bigdata.flipkart-data`";

        // Query for filtered products count
        String filteredProductsCountQuery = "SELECT COUNT(*) AS count FROM `virtualization-and-cloud.amazon_bigdata.flipkart-data` WHERE 1=1" +
                (keyword != null && !keyword.trim().isEmpty() ? " AND LOWER(product_name) LIKE LOWER('%' || @keyword || '%')" : "") +
                (category != null && !category.trim().isEmpty() ? " AND LOWER(product_category_tree) LIKE LOWER('%' || @category || '%')" : "") +
                " AND discounted_price BETWEEN @priceGte AND @priceLte" +
                " AND CAST(overall_rating AS FLOAT64) >= @ratingsGte";

        // Main query to fetch products
        String productsQuery = "SELECT * FROM `virtualization-and-cloud.amazon_bigdata.flipkart-data` WHERE 1=1" +
                (keyword != null && !keyword.trim().isEmpty() ? " AND LOWER(product_name) LIKE LOWER('%' || @keyword || '%')" : "") +
                (category != null && !category.trim().isEmpty() ? " AND LOWER(product_category_tree) LIKE LOWER('%' || @category || '%')" : "") +
                " AND discounted_price BETWEEN @priceGte AND @priceLte" +
                " AND CAST(overall_rating AS FLOAT64) >= @ratingsGte" +
                " LIMIT @pageSize OFFSET @offset";

        try {
            // Fetch total products count
            QueryJobConfiguration totalCountConfig = QueryJobConfiguration.newBuilder(totalProductsQuery).build();
            TableResult totalResult = executeQuery(totalCountConfig);
            int totalProductsCount = totalResult.iterateAll().iterator().next().get("count").getNumericValue().intValue();

            // Fetch filtered products count
            QueryJobConfiguration filteredCountConfig = QueryJobConfiguration.newBuilder(filteredProductsCountQuery)
                    .addNamedParameter("keyword", QueryParameterValue.string(keyword))
                    .addNamedParameter("category", QueryParameterValue.string(category))
                    .addNamedParameter("priceGte", QueryParameterValue.float64(priceGte))
                    .addNamedParameter("priceLte", QueryParameterValue.float64(priceLte))
                    .addNamedParameter("ratingsGte", QueryParameterValue.float64(ratingsGte))
                    .build();
            TableResult filteredResult = executeQuery(filteredCountConfig);
            int filteredProductsCount = filteredResult.iterateAll().iterator().next().get("count").getNumericValue().intValue();

            // Fetch products
            QueryJobConfiguration productsConfig = QueryJobConfiguration.newBuilder(productsQuery)
                    .addNamedParameter("keyword", QueryParameterValue.string(keyword))
                    .addNamedParameter("category", QueryParameterValue.string(category))
                    .addNamedParameter("priceGte", QueryParameterValue.float64(priceGte))
                    .addNamedParameter("priceLte", QueryParameterValue.float64(priceLte))
                    .addNamedParameter("ratingsGte", QueryParameterValue.float64(ratingsGte))
                    .addNamedParameter("pageSize", QueryParameterValue.int64(pageSize))
                    .addNamedParameter("offset", QueryParameterValue.int64(offset))
                    .build();
            TableResult productResult = executeQuery(productsConfig);

            List<Product> products = new ArrayList<>();
            for (FieldValueList row : productResult.iterateAll()) {
                products.add(mapRowToProduct(row));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("products", products);
            response.put("productsCount", totalProductsCount);
            response.put("filteredProductsCount", filteredProductsCount);
            response.put("resultPerPage", pageSize);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching filtered products: " + e.getMessage(), e);
        }
    }

}

