package com.pricepulse.backend.service.crawler.impl;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.AbstractCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 京东爬虫服务 - API 版本
 * 使用京东价格 API: https://p.3.cn/prices/mgets?skuIds=J_商品 ID
 */
@Service
@ConditionalOnProperty(name = "crawler.jd.use-api", havingValue = "true", matchIfMissing = false)
@Slf4j
public class JDApiCrawlerServiceImpl extends AbstractCrawlerService {

    private static final String JD_PRICE_API_URL = "https://p.3.cn/prices/mgets?skuIds=J_{0}";

    private final RestTemplate restTemplate;

    @Value("${crawler.jd.api.enabled:true}")
    private boolean apiEnabled;

    public JDApiCrawlerServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    protected PriceInfo doCrawlPrice(String productId, String url) {
        if (!apiEnabled) {
            log.warn("京东 API 未启用，返回模拟数据");
            return generateMockPrice(productId);
        }

        try {
            String apiUrl = JD_PRICE_API_URL.replace("{0}", productId);
            log.info("调用京东价格 API: {}", apiUrl);

            // 调用京东价格 API
            String response = restTemplate.getForObject(apiUrl, String.class);

            if (response == null || response.isEmpty()) {
                log.warn("API 返回空响应，使用模拟数据");
                return generateMockPrice(productId);
            }

            // 解析 API 响应
            return parseApiResponse(response, productId);

        } catch (Exception e) {
            log.error("调用京东 API 失败，使用模拟数据：{}", e.getMessage());
            return generateMockPrice(productId);
        }
    }

    /**
     * 解析 API 响应
     * 京东 API 返回格式示例：[{"id":"J_12345","p":"9999.00","m":"8999.00"}]
     */
    private PriceInfo parseApiResponse(String response, String productId) {
        try {
            // 简单的 JSON 解析（实际项目中建议使用 Jackson 或 FastJSON）
            // 这里简化处理，假设返回格式为 [{"id":"J_xxx","p":"xxx","m":"xxx"}]

            BigDecimal currentPrice = new BigDecimal("9999.00");
            BigDecimal originalPrice = new BigDecimal("8999.00");

            // TODO: 完善 JSON 解析逻辑
            // 可以使用 org.json.JSONObject 或 com.fasterxml.jackson.databind.JsonNode

            BigDecimal discountRate = currentPrice.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .originalPrice(originalPrice)
                    .discountRate(discountRate)
                    .inStock(true)
                    .title("京东商品-" + productId)
                    .build();

        } catch (Exception e) {
            log.error("解析 API 响应失败：{}", e.getMessage());
            return generateMockPrice(productId);
        }
    }

    /**
     * 生成模拟价格（降级方案）
     */
    private PriceInfo generateMockPrice(String productId) {
        java.util.Random random = new java.util.Random(productId.hashCode());
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
