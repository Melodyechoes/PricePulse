package com.pricepulse.backend.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceHistory {
    private Long id;
    private Long productId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountRate;
    private String currency;
    private LocalDateTime checkedAt;
    private String source;
}
