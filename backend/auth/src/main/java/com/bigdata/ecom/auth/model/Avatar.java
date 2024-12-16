package com.bigdata.ecom.auth.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Avatar {
    private String publicId;
    private String url;
}