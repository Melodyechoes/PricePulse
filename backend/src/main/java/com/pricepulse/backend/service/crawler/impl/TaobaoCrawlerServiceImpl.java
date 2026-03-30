package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Slf4j
public class TaobaoCrawlerServiceImpl implements CrawlerService {

    @Override
    public PriceInfo crawlPrice(String url) {
        try {
            log.info("【淘宝爬虫】开始抓取商品价格：{}", url);

            // 【演示模式】生成模拟价格数据
            // 实际项目中需要接入淘宝联盟 API

            String productId = extractProductId(url);
            log.info("提取到商品 ID: {}", productId);

            // 基于商品 ID 生成稳定的模拟价格
            Random random = new Random(productId.hashCode());
            BigDecimal basePrice = new BigDecimal(100 + random.nextInt(4900)); // 100-4999 元

            // 模拟淘宝促销活动（9 折 -95 折）
            double discountFactor = 0.9 + (random.nextDouble() * 0.05);
            BigDecimal currentPrice = basePrice.multiply(new BigDecimal(String.valueOf(discountFactor)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal discountRate = currentPrice.divide(basePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            log.info("【淘宝爬虫】模拟价格：原价 {} -> 现价 {}, 折扣率 {}%",
                    basePrice, currentPrice, discountRate);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .originalPrice(basePrice)
                    .discountRate(discountRate)
                    .inStock(true)
                    .title("淘宝商品-" + productId)
                    .build();

        } catch (Exception e) {
            log.error("【淘宝爬虫】抓取失败：{}", url, e);
            return PriceInfo.builder()
                    .errorMessage("抓取失败：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("taobao.com") || url.contains("tmall.com");
    }

    @Override
    public String getPlatform() {
        return "taobao";
    }

    private String extractProductId(String url) {
        // 从 URL 中提取商品 ID
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String[] parts = urlObj.getPath().split("/");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    return part;
                }
            }
        } catch (Exception e) {
            log.warn("URL 解析失败", e);
        }
        return "未知商品";
    }
}
