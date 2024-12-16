package com.bigdata.ecom.products.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewedProductEvent {
    private String userId;
    private String productId;
    private String productName;
    private List<String> category;
    private String brandName;
    private double price;
    private double discountedPrice;
    private long timestamp;
    private String imageUrl;
    private String region;
    private int stock;
    private double ratings;
    private String eventType;

    public ViewedProductEvent(String userId, String productId, long timestamp) {
        this.userId = userId;
        this.productId = productId;
        this.timestamp = timestamp;
    }
    public ViewedProductEvent(String userId, String productId, String productName, List<String> category, String brandName, double price, double discountedPrice, long timestamp, String imageUrl, String region, int stock, double ratings, String eventType) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.brandName = brandName;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.region = region;
        this.stock = stock;
        this.ratings = ratings;
        this.eventType = eventType;
    }

}
