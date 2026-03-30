package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Slf4j
public class JDCrawlerServiceImpl implements CrawlerService {

    @Override
    public PriceInfo crawlPrice(String url) {
        try {
            log.info("【京东爬虫】开始抓取商品价格：{}", url);

            // 【演示模式】生成模拟价格数据
            // 实际项目中会调用京东价格 API: https://p.3.cn/prices/mgets?skuIds=J_商品ID

            String productId = extractProductId(url);
            log.info("提取到商品 ID: {}", productId);

            // 基于商品 ID 生成一个"伪随机"但稳定的价格
            Random random = new Random(productId.hashCode());
            BigDecimal basePrice = new BigDecimal(500 + random.nextInt(9500)); // 500-9999 元

            // 模拟促销折扣（85 折 -95 折）
            double discountFactor = 0.85 + (random.nextDouble() * 0.1);
            BigDecimal currentPrice = basePrice.multiply(new BigDecimal(String.valueOf(discountFactor)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal discountRate = currentPrice.divide(basePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            log.info("【京东爬虫】模拟价格：原价 {} -> 现价 {}, 折扣率 {}%",
                    basePrice, currentPrice, discountRate);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .originalPrice(basePrice)
                    .discountRate(discountRate)
                    .inStock(true)
                    .title("京东商品-" + productId)
                    .build();

        } catch (Exception e) {
            log.error("【京东爬虫】抓取失败：{}", url, e);
            return PriceInfo.builder()
                    .errorMessage("抓取失败：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("jd.com") || url.contains("360buy.com");
    }

    @Override
    public String getPlatform() {
        return "jd";
    }

    private String extractProductId(String url) {
        // 从 URL 中提取商品 ID
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/(\\d{10,})(\\.html)?");
        java.util.regex.Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "未知商品";
    }
}
