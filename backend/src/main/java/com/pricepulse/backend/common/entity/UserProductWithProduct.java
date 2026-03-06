// src/main/java/com/pricepulse/backend/common/entity/UserProductWithProduct.java
package com.pricepulse.backend.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProductWithProduct extends UserProduct {
    private String productName;
    private BigDecimal productCurrentPrice;
    private String productImageUrl;
}
