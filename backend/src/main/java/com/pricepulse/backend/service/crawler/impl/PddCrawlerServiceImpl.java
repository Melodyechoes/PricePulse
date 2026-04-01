package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.AbstractCrawlerService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class PddCrawlerServiceImpl extends AbstractCrawlerService {

    @Override
    protected PriceInfo doCrawlPrice(String productId, String url) {
        Random random = new Random(productId.hashCode());
        BigDecimal basePrice = new BigDecimal(50 + random.nextInt(1950));

        double discountFactor = 0.8 + (random.nextDouble() * 0.1);
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
                .title("拼多多商品-" + productId)
                .build();
    }

    @Override
    protected String getPlatformName() {
        return "拼多多";
    }

    @Override
    public boolean supports(String url) {
        return url.contains("yangkeduo.com") || url.contains("pinduoduo.com");
    }

    @Override
    public String getPlatform() {
        return "pdd";
    }

    @Override
    protected String extractProductId(String url) {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String query = urlObj.getQuery();
            if (query != null && query.contains("goods_id=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("goods_id=")) {
                        return param.substring(11);
                    }
                }
            }
        } catch (Exception e) {
            // Deleted:log.warn("URL 解析失败", e);
        }
        return "未知商品";
    }
}
