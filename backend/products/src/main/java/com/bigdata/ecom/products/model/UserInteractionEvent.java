package com.bigdata.ecom.products.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionEvent {
    private String userId;
    private String productId;
    private String eventType;
    private long timestamp;
}
