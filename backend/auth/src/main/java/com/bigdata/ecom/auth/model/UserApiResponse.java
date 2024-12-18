package com.bigdata.ecom.auth.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserApiResponse {
    private Boolean success;
    private String message;

    public UserApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
