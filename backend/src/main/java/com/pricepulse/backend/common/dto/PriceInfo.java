package com.pricepulse.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceInfo {

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 是否有货
     */
    private Boolean inStock;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 错误信息（如果抓取失败）
     */
    private String errorMessage;
}
