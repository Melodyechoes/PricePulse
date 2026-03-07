package com.pricepulse.backend.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationEntity {
    private Long id;
    private Long userId;
    private String message;
    private String type; // PRICE_DROP, STOCK_IN, etc.
    private Integer isRead; // 0-未读，1-已读
    private Long relatedProductId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
