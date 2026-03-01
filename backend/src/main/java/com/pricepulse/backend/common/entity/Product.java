package com.pricepulse.backend.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String name;
    private String url;
    private String imageUrl;
    private PlatformEnum platform;
    private String platformId;
    private String brand;
    private String category;
    private BigDecimal currentPrice;
    private BigDecimal originalPrice;
    private BigDecimal discountRate;
    private Integer salesCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer stockStatus;
    private LocalDateTime lastChecked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer status;
}
