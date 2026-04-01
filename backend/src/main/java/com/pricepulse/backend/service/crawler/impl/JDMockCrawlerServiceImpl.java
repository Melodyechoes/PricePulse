package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.AbstractCrawlerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * 京东爬虫服务 - 模拟数据版本
 */
@Service
@ConditionalOnProperty(name = "crawler.jd.use-api", havingValue = "false", matchIfMissing = true)
public class JDMockCrawlerServiceImpl extends AbstractCrawlerService {

    @Override
    protected PriceInfo doCrawlPrice(String productId, String url) {
        Random random = new Random(productId.hashCode());
        BigDecimal basePrice = new BigDecimal(500 + random.nextInt(9500));

        double discountFactor = 0.85 + (random.nextDouble() * 0.1);
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
                .title("京东商品-" + productId)
                .build();
    }

    @Override
    protected String getPlatformName() {
        return "京东";
    }

    @Override
    public boolean supports(String url) {
        return url.contains("jd.com") || url.contains("360buy.com");
    }

    @Override
    public String getPlatform() {
        return "jd";
    }

    @Override
    protected String extractProductId(String url) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/(\\d{10,})(\\.html)?");
        java.util.regex.Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "未知商品";
    }
}
