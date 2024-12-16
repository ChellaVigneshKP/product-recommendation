package com.bigdata.ecom.products.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FormattedProduct {
    @JsonProperty("_id")
    private String id;
    private String name;
    private String description;
    private double price;
    private double cuttedPrice;
    private String category;
    private int stock;
    private int warranty;
    private double ratings;
    private int numOfReviews;
    private List<Map<String, Object>> reviews;
    private String createdAt;
    private Map<String, Object> brand;
    private List<Map<String, Object>> images;
    private List<Map<String, Object>> specifications;
    private List<String> highlights;
    @JsonProperty("__v")
    private int version; // __v
}