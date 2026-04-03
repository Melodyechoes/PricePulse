package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.AbstractCrawlerService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class TaobaoApiServiceImpl extends AbstractCrawlerService {

    @Override
    protected PriceInfo doCrawlPrice(String productId, String url) {
        Random random = new Random(productId.hashCode());
        BigDecimal basePrice = new BigDecimal(100 + random.nextInt(4900));

        double discountFactor = 0.9 + (random.nextDouble() * 0.05);
        BigDecimal currentPrice = basePrice.multiply(new BigDecimal(String.valueOf(discountFactor)))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal discountRate = currentPrice.divide(basePrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        return PriceInfo.builder()
                .currentPrice(currentPrice)
                .originalPrice(basePrice)
                .discountRate(discountRate)
                .inStock(true)
                .title("淘宝商品-" + productId)
                .build();
    }

    @Override
    protected String getPlatformName() {
        return "淘宝";
    }

    @Override
    public boolean supports(String url) {
        return url.contains("taobao.com") || url.contains("tmall.com");
    }

    @Override
    public String getPlatform() {
        return "taobao";
    }

    @Override
    protected String extractProductId(String url) {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String[] parts = urlObj.getPath().split("/");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    return part;
                }
            }
        } catch (Exception e) {
            // Deleted:log.warn("URL 解析失败", e);
        }
        return "未知商品";
    }
}
