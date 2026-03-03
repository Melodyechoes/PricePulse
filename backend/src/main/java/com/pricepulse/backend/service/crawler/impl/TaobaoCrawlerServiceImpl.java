package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TaobaoCrawlerServiceImpl implements CrawlerService {

    private static final Pattern PRICE_PATTERN = Pattern.compile("\"price\":\"(\\d+\\.?\\d*)\"");
    private static final Pattern ORIGINAL_PRICE_PATTERN = Pattern.compile("\"originalPrice\":\"(\\d+\\.?\\d*)\"");
    private static final Pattern TITLE_PATTERN = Pattern.compile("\"title\":\"([^\"]+)\"");

    @Override
    public PriceInfo crawlPrice(String url) {
        try {
            log.info("开始抓取淘宝/天猫商品价格: {}", url);

            // 模拟网页请求（实际项目中需要处理反爬机制）
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            // 解析价格（这里需要根据实际页面结构调整）
            String pageContent = doc.html();

            BigDecimal currentPrice = extractPrice(pageContent, PRICE_PATTERN);
            BigDecimal originalPrice = extractPrice(pageContent, ORIGINAL_PRICE_PATTERN);
            String title = extractText(pageContent, TITLE_PATTERN);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .originalPrice(originalPrice)
                    .discountRate(calculateDiscountRate(currentPrice, originalPrice))
                    .inStock(currentPrice != null)
                    .title(title)
                    .build();

        } catch (Exception e) {
            log.error("抓取淘宝/天猫价格失败: {}", url, e);
            return PriceInfo.builder()
                    .errorMessage("抓取失败：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("taobao.com") || url.contains("tmall.com");
    }

    private BigDecimal extractPrice(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("价格解析失败", e);
            }
        }
        return null;
    }

    private String extractText(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private BigDecimal calculateDiscountRate(BigDecimal current, BigDecimal original) {
        if (current == null || original == null || original.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.divide(original, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
