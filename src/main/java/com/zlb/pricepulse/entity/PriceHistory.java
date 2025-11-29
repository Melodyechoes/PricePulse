package com.zlb.pricepulse.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceHistory {
    private Long id;
    private Long productId;
    private BigDecimal price;
    private LocalDateTime createdAt;
}
