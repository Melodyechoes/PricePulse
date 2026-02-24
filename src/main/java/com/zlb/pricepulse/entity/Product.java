package com.zlb.pricepulse.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Product {
    private long id;
    private String name;
    private String url;
    private String imageUrl;
    private LocalDateTime createdAt;
}
