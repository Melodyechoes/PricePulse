package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Slf4j
public class PddCrawlerServiceImpl implements CrawlerService {

    @Override
    public PriceInfo crawlPrice(String url) {
        try {
            log.info("【拼多多爬虫】开始抓取商品价格：{}", url);

            // 【演示模式】生成模拟价格数据
            // 实际项目中需要接入拼多多开放平台 API

            String productId = extractProductId(url);
            log.info("提取到商品 ID: {}", productId);

            // 拼多多以低价著称（50-2000 元）
            Random random = new Random(productId.hashCode());
            BigDecimal basePrice = new BigDecimal(50 + random.nextInt(1950)); // 50-1999 元

            // 模拟拼多多的百亿补贴（8 折 -9 折）
            double discountFactor = 0.8 + (random.nextDouble() * 0.1);
            BigDecimal currentPrice = basePrice.multiply(new BigDecimal(String.valueOf(discountFactor)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal discountRate = currentPrice.divide(basePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            log.info("【拼多多爬虫】模拟价格：原价 {} -> 现价 {}, 折扣率 {}%",
                    basePrice, currentPrice, discountRate);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .originalPrice(basePrice)
                    .discountRate(discountRate)
                    .inStock(true)
                    .title("拼多多商品-" + productId)
                    .build();

        } catch (Exception e) {
            log.error("【拼多多爬虫】抓取失败：{}", url, e);
            return PriceInfo.builder()
                    .errorMessage("抓取失败：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("yangkeduo.com") || url.contains("pinduoduo.com");
    }

    @Override
    public String getPlatform() {
        return "pdd";
    }

    private String extractProductId(String url) {
        // 从 URL 中提取商品 ID
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
            log.warn("URL 解析失败", e);
        }
        return "未知商品";
    }
}
