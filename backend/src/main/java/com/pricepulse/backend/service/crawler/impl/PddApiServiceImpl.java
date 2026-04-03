package com.pricepulse.backend.service.crawler.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.AbstractCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ConditionalOnProperty(name = "crawler.pdd.use-api", havingValue = "true", matchIfMissing = false)
@Slf4j
public class PddApiServiceImpl extends AbstractCrawlerService {

    private static final String PDD_API_URL = "http://gw-api.pinduoduo.com/api/router";

    @Value("${crawler.pdd.api.client-id}")
    private String clientId;

    @Value("${crawler.pdd.api.client-secret}")
    private String clientSecret;

    @Value("${crawler.pdd.api.pid:}")
    private String pid;

    @Value("${crawler.pdd.api.enabled:true}")
    private boolean apiEnabled;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PddApiServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected PriceInfo doCrawlPrice(String productId, String url) {
        if (!apiEnabled) {
            log.warn("拼多多 API 未启用，返回模拟数据");
            return generateMockPrice(productId);
        }

        try {
            log.info("【拼多多爬虫】开始抓取商品详情，标识：{}, URL: {}", productId, url);

            if (pid == null || pid.isEmpty()) {
                log.warn("PID 未配置，无法调用 API，使用模拟数据");
                return generateMockPrice(productId);
            }

            if (productId != null && productId.length() >= 28) {
                log.info("使用 goods_sign 直接查询：{}", productId);
                PriceInfo priceInfo = queryByGoodsSign(productId);
                if (priceInfo != null) {
                    return priceInfo;
                }
            }

            log.info("goods_sign 长度不足，尝试通过搜索获取真实的 goodsSign");
            String realGoodsSign = searchGoodsSign(productId);
            if (realGoodsSign != null) {
                log.info("搜索到真实的 goodsSign: {}", realGoodsSign);
                PriceInfo priceInfo = queryByGoodsSign(realGoodsSign);
                if (priceInfo != null) {
                    return priceInfo;
                }
            }

            log.warn("API 查询失败，尝试从移动端页面抓取");
            return crawlFromMobilePage(url);

        } catch (Exception e) {
            log.error("调用 API 失败，尝试从移动端页面抓取：{}", e.getMessage());
            return crawlFromMobilePage(url);
        }
    }

    private String searchGoodsSign(String keyword) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "pdd.ddk.goods.search");
        params.put("client_id", clientId);
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("keyword", keyword);
        params.put("page", 1);
        params.put("page_size", 20);
        params.put("pid", pid);

        String signature = generateSignature(params, clientSecret);
        params.put("sign", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = createRequestEntity(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(PDD_API_URL, request, String.class);

        log.info("搜索 API 响应状态码：{}", response.getStatusCode());
        log.info("搜索 API 响应内容：{}", response.getBody());

        if (response.getBody() == null || response.getBody().isEmpty()) {
            return null;
        }

        JsonNode rootNode = objectMapper.readTree(response.getBody());

        if (rootNode.has("error_response")) {
            return null;
        }

        JsonNode goodsList = rootNode.path("goods_search_response").path("goods_list");
        if (goodsList.isMissingNode() || !goodsList.isArray() || goodsList.size() == 0) {
            return null;
        }

        return goodsList.get(0).path("goods_sign").asText();
    }

    private PriceInfo queryByGoodsSign(String goodsSign) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "pdd.ddk.goods.detail");
        params.put("client_id", clientId);
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("goods_img_type", 1);
        params.put("goods_sign", goodsSign);
        params.put("pid", pid);

        String signature = generateSignature(params, clientSecret);

        log.info("请求参数列表：");
        params.forEach((key, value) -> log.info("  {} = {}", key, value));
        log.info("生成的 API 签名：{}", signature);

        params.put("sign", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = createRequestEntity(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(PDD_API_URL, request, String.class);

        log.info("API 响应状态码：{}", response.getStatusCode());
        log.info("API 响应内容：{}", response.getBody());

        if (response.getBody() == null || response.getBody().isEmpty()) {
            return null;
        }

        JsonNode rootNode = objectMapper.readTree(response.getBody());

        if (rootNode.has("error_response")) {
            JsonNode errorNode = rootNode.get("error_response");
            String errorMsg = errorNode.has("sub_msg") ? errorNode.get("sub_msg").asText() : "未知错误";
            log.error("API 返回错误：{}", errorMsg);
            return null;
        }

        JsonNode goodsDetailNode = rootNode.path("goods_detail_response").path("goods_details");
        if (goodsDetailNode.isMissingNode() || !goodsDetailNode.isArray() || goodsDetailNode.size() == 0) {
            return null;
        }

        goodsDetailNode = goodsDetailNode.get(0);
        return parseApiResponse(goodsDetailNode, goodsSign);
    }

    private String generateSignature(Map<String, Object> params, String secret) throws Exception {
        StringBuilder sb = new StringBuilder();

        params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    sb.append(entry.getKey()).append(entry.getValue());
                });

        String signSource = secret + sb.toString() + secret;

        log.debug("签名原文：{}", signSource);

        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(signSource.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        String sign = hexString.toString().toUpperCase();
        log.debug("最终签名：{}", sign);

        return sign;
    }

    private HttpEntity<MultiValueMap<String, String>> createRequestEntity(
            Map<String, Object> params, HttpHeaders headers) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        params.forEach((key, value) -> formData.add(key, String.valueOf(value)));
        return new HttpEntity<>(formData, headers);
    }

    private PriceInfo parseApiResponse(JsonNode goodsDetail, String productId) throws Exception {
        try {
            JsonNode minGroupPriceNode = goodsDetail.get("min_group_price");
            JsonNode minNormalPriceNode = goodsDetail.get("min_normal_price");

            BigDecimal currentPrice = minGroupPriceNode != null && !minGroupPriceNode.isNull()
                    ? new BigDecimal(minGroupPriceNode.asLong()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)
                    : null;

            BigDecimal originalPrice = minNormalPriceNode != null && !minNormalPriceNode.isNull()
                    ? new BigDecimal(minNormalPriceNode.asLong()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)
                    : null;

            if (currentPrice == null && originalPrice == null) {
                log.warn("价格为空，使用模拟数据");
                return generateMockPrice(productId);
            }

            if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
                originalPrice = currentPrice != null ? currentPrice.multiply(new BigDecimal("1.2")).setScale(2, BigDecimal.ROUND_HALF_UP) : new BigDecimal("0");
            }

            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) == 0) {
                currentPrice = originalPrice.multiply(new BigDecimal("0.9")).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            BigDecimal discountRate = currentPrice.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            JsonNode goodsNameNode = goodsDetail.get("goods_name");
            String title = goodsNameNode != null && !goodsNameNode.isNull()
                    ? goodsNameNode.asText()
                    : "拼多多商品-" + productId;

            JsonNode imageUrlNode = goodsDetail.get("goods_image_url");
            String imageUrl = imageUrlNode != null && !imageUrlNode.isNull()
                    ? imageUrlNode.asText()
                    : null;

            if (imageUrl == null || imageUrl.isEmpty()) {
                JsonNode thumbnailNode = goodsDetail.get("goods_thumbnail_url");
                imageUrl = thumbnailNode != null && !thumbnailNode.isNull() ? thumbnailNode.asText() : null;
            }

            log.info("API 解析成功 - 价格：{}, 原价：{}, 标题：{}", currentPrice, originalPrice, title);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .originalPrice(originalPrice)
                    .discountRate(discountRate)
                    .inStock(true)
                    .title(title)
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            log.error("解析 API 响应失败：{}", e.getMessage(), e);
            return generateMockPrice(productId);
        }
    }

    private PriceInfo crawlFromMobilePage(String url) {
        try {
            log.info("开始从移动端页面抓取：{}", url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1")
                    .timeout(15000)
                    .followRedirects(true)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                    .get();

            String pageHtml = doc.html();
            log.info("页面内容长度：{} 字节", pageHtml.length());

            try {
                java.nio.file.Files.write(
                        java.nio.file.Paths.get("pdd_page_debug.html"),
                        pageHtml.getBytes("UTF-8")
                );
                log.info("页面已保存到 pdd_page_debug.html");
            } catch (Exception e) {
                log.debug("保存页面失败：{}", e.getMessage());
            }

            BigDecimal currentPrice = extractPrice(pageHtml, "\"minGroupPrice\":\\s*(\\d+)");
            if (currentPrice == null) {
                currentPrice = extractPrice(pageHtml, "\"finalPrice\":\\s*(\\d+)");
            }
            if (currentPrice == null) {
                currentPrice = extractPrice(pageHtml, "\"price\":\\s*(\\d+)");
            }

            BigDecimal originalPrice = extractPrice(pageHtml, "\"minNormalPrice\":\\s*(\\d+)");
            if (originalPrice == null) {
                originalPrice = extractPrice(pageHtml, "\"marketPrice\":\\s*(\\d+)");
            }

            String title = extractString(pageHtml, "\"goodsName\":\\s*\"([^\"]+)\"");
            if (title == null) {
                title = extractString(pageHtml, "\"goodsDesc\":\\s*\"([^\"]+)\"");
            }

            String imageUrl = extractString(pageHtml, "\"hdThumbImage\":\\s*\"([^\"]+)\"");
            if (imageUrl == null) {
                imageUrl = extractString(pageHtml, "\"thumbUrl\":\\s*\"([^\"]+)\"");
            }
            if (imageUrl != null) {
                imageUrl = imageUrl.replaceAll("\\\\/", "/");
            }

            if (currentPrice == null && originalPrice == null && title == null) {
                log.warn("页面中未找到商品信息，使用模拟数据");
                return generateMockPrice("pdd_mobile");
            }

            if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
                originalPrice = currentPrice != null ? currentPrice.multiply(new BigDecimal("1.2")).setScale(2, BigDecimal.ROUND_HALF_UP) : new BigDecimal("0");
            }

            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) == 0) {
                currentPrice = originalPrice.multiply(new BigDecimal("0.9")).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            BigDecimal discountRate = currentPrice.divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            if (title == null || title.isEmpty()) {
                title = "拼多多商品-" + System.currentTimeMillis();
            }

            log.info("移动端页面抓取成功 - 价格：{}, 原价：{}, 标题：{}", currentPrice, originalPrice, title);

            return PriceInfo.builder()
                    .currentPrice(currentPrice)
                    .originalPrice(originalPrice)
                    .discountRate(discountRate)
                    .inStock(true)
                    .title(title)
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            log.error("从移动端页面抓取失败：{}", e.getMessage(), e);
            return generateMockPrice("pdd_mobile_error");
        }
    }

    private BigDecimal extractPrice(String html, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                BigDecimal price = new BigDecimal(matcher.group(1)).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                log.info("通过正则 [{}] 提取到价格：{}", regex, price);
                return price;
            }
        } catch (Exception e) {
            log.debug("提取价格失败：{}", e.getMessage());
        }
        return null;
    }

    private String extractString(String html, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                String value = matcher.group(1);
                log.debug("通过正则 [{}] 提取到字符串：{}", regex, value);
                return value;
            }
        } catch (Exception e) {
            log.debug("提取字符串失败：{}", e.getMessage());
        }
        return null;
    }

    private PriceInfo generateMockPrice(String productId) {
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
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("ps=")) {
                        String goodsSign = param.substring(3);
                        log.info("从 URL 中提取到 goods_sign(ps): {}", goodsSign);
                        return goodsSign;
                    }
                    if (param.startsWith("goods_id=")) {
                        String goodsId = param.substring(11);
                        log.info("从 URL 中提取到 goods_id: {}", goodsId);
                        return goodsId;
                    }
                    if (param.startsWith("goodsSign=")) {
                        String goodsSign = param.substring(12);
                        log.info("从 URL 中提取到 goodsSign: {}", goodsSign);
                        return goodsSign;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("URL 解析失败：{}", e.getMessage());
        }

        try {
            java.net.URL urlObj = new java.net.URL(url);
            String path = urlObj.getPath();
            if (path.endsWith(".html")) {
                String[] segments = path.split("/");
                if (segments.length > 0) {
                    String lastSegment = segments[segments.length - 1];
                    String goodsSign = lastSegment.replace(".html", "");
                    if (!goodsSign.isEmpty()) {
                        log.info("从路径中提取到 goods_sign: {}", goodsSign);
                        return goodsSign;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("路径解析失败：{}", e.getMessage());
        }

        log.warn("无法从 URL 中提取商品 ID: {}", url);
        return "未知商品";
    }
}
