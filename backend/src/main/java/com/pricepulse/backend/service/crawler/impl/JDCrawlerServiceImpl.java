package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class JDCrawlerServiceImpl implements CrawlerService {

    @Override
    public PriceInfo crawlPrice(String url) {
        try {
            log.info("开始抓取京东商品价格: {}", url);

            // 提取商品ID
            String productId = extractProductId(url);

            // 构建价格API URL
            String priceUrl = "https://p.3.cn/prices/mgets?skuIds=J_" + productId;

            Document doc = Jsoup.connect(priceUrl)
                    .ignoreContentType(true)
                    .timeout(5000)
                    .get();

            String jsonContent = doc.body().text();

            // 解析JSON获取价格（简化处理）
            BigDecimal currentPrice = parsePriceFromJson(jsonContent);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .inStock(currentPrice != null)
                    .build();

        } catch (Exception e) {
            log.error("抓取京东价格失败: {}", url, e);
            return PriceInfo.builder()
                    .errorMessage("抓取失败：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("jd.com") || url.contains("360buy.com");
    }

    private String extractProductId(String url) {
        // 从URL中提取商品ID
        if (url.contains("/")) {
            String[] parts = url.split("/");
            for (String part : parts) {
                if (part.matches("\\d+") && part.length() >= 6) {
                    return part;
                }
            }
        }
        return null;
    }

    private BigDecimal parsePriceFromJson(String json) {
        // 简化JSON解析（实际项目建议使用Jackson或Gson）
        if (json.contains("\"p\":\"")) {
            int start = json.indexOf("\"p\":\"") + 4;
            int end = json.indexOf("\"", start);
            if (start < end) {
                try {
                    return new BigDecimal(json.substring(start, end));
                } catch (NumberFormatException e) {
                    log.warn("价格解析失败", e);
                }
            }
        }
        return null;
    }
}
