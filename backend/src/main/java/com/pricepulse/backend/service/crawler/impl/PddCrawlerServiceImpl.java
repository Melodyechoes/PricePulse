package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class PddCrawlerServiceImpl implements CrawlerService {

    @Override
    public PriceInfo crawlPrice(String url) {
        try {
            log.info("开始抓取拼多多商品价格：{}", url);

            // 拼多多移动端页面
            String mobileUrl = convertToMobileUrl(url);

            Document doc = Jsoup.connect(mobileUrl)
                    .userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X)")
                    .timeout(5000)
                    .get();

            // 尝试多种选择器获取价格
            BigDecimal currentPrice = extractPrice(doc);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .inStock(currentPrice != null)
                    .build();

        } catch (Exception e) {
            log.error("抓取拼多多价格失败：{}", url, e);
            return PriceInfo.builder()
                    .errorMessage("抓取失败：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("yangkeduo.com") || url.contains("pinduoduo.com");
    }

    private String convertToMobileUrl(String url) {
        // 转换为移动端链接
        if (url.contains("mobile.yangkeduo.com")) {
            return url;
        }
        return url.replace("mobile.", "").replace("www.", "mobile.");
    }

    private BigDecimal extractPrice(Document doc) {
        // 尝试不同的价格选择器
        String[] selectors = {
                ".phone-price-page .normal-price",
                ".spec-item .normal-price",
                ".goods-price",
                "[class*='price']"
        };

        for (String selector : selectors) {
            try {
                String priceText = doc.select(selector).first().text();
                BigDecimal price = parsePrice(priceText);
                if (price != null) {
                    return price;
                }
            } catch (Exception e) {
                // 继续尝试下一个选择器
            }
        }
        return null;
    }

    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }

        try {
            // 移除货币符号和空格
            String cleanPrice = priceText.replaceAll("[￥¥\\s,]", "");

            // 提取数字
            int start = cleanPrice.indexOf('¥') != -1 ? cleanPrice.indexOf('¥') + 1 : 0;
            String numberStr = cleanPrice.substring(start).trim();

            return new BigDecimal(numberStr);
        } catch (Exception e) {
            log.warn("价格解析失败：{}", priceText, e);
            return null;
        }
    }
}
