package com.bigdata.ecom.auth.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
    public LoginRequest(String email, String password){
        this.email = email;
        this.password = password;
    }
}
