package com.bigdata.ecom.products.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    @JsonProperty("uniq_id") // Maps `uniq_id` to `id`
    private String id;

    @JsonProperty("product_name") // Maps `product_name` to `name`
    private String name;

    private String description;

    @JsonProperty("retail_price") // Maps `retail_price` to `price`
    private double price;

    @JsonProperty("product_category_tree") // Maps `product_category_tree` to `category`
    private String category;

    private String brand;

    @JsonProperty("image") // Maps `image` to `imageUrl`
    private String imageUrl;

    private Timestamp createdAt;

    @JsonProperty("product_specifications") // Maps `product_specifications` to `productSpecifications`
    private String productSpecifications;

    @JsonProperty("discounted_price") // Maps `discounted_price` to `discounted_price`
    private double discounted_price;

    @JsonProperty("overall_rating") // Maps `overall_rating` to `ratings`
    private double ratings;
}
