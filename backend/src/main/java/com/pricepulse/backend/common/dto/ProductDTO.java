package com.pricepulse.backend.common.dto;

import com.pricepulse.backend.common.entity.PlatformEnum;
import lombok.Data;
import jakarta.validation.constraints.*;  // 使用jakarta.validation替代javax.validation

@Data
public class ProductDTO {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 255, message = "商品名称长度不能超过255个字符")
    private String name;

    @NotBlank(message = "商品链接不能为空")
    private String url;

    private String imageUrl;

    @NotNull(message = "平台类型不能为空")
    private PlatformEnum platform;

    private String platformId;

    @Size(max = 100, message = "品牌名称长度不能超过100个字符")
    private String brand;

    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    private String category;

    @DecimalMin(value = "0.01", message = "当前价格必须大于0")
    private Double currentPrice;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private Double originalPrice;

    @DecimalMin(value = "0", message = "折扣率不能小于0")
    @DecimalMax(value = "100", message = "折扣率不能大于100")
    private Double discountRate;

    @Min(value = 0, message = "销量不能小于0")
    private Integer salesCount;

    @DecimalMin(value = "0", message = "评分不能小于0")
    @DecimalMax(value = "5", message = "评分不能大于5")
    private Double rating;

    @Min(value = 0, message = "评论数不能小于0")
    private Integer reviewCount;

    @Min(value = 0, message = "库存状态只能是0或1")
    @Max(value = 1, message = "库存状态只能是0或1")
    private Integer stockStatus;
}
