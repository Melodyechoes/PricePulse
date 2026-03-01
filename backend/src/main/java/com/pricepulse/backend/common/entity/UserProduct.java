package com.pricepulse.backend.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProduct {
    private Long id;
    private Long userId;
    private Long productId;
    private BigDecimal targetPrice;
    private Integer notificationEnabled;
    private BigDecimal priceDropThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
