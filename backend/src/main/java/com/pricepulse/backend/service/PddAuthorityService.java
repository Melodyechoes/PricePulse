package com.pricepulse.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PddAuthorityService {

    private static final String PDD_API_URL = "http://gw-api.pinduoduo.com/api/router";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String clientId;
    private String clientSecret;

    public PddAuthorityService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void setCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * 生成授权备案链接
     */
    public String generateAuthorityUrl(String pid) throws Exception {
        log.info("开始生成授权备案链接，PID: {}", pid);

        Map<String, Object> params = new HashMap<>();
        params.put("type", "pdd.ddk.rp.prom.url.generate");
        params.put("client_id", clientId);
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("channel_type", 10);
        params.put("p_id_list", "[\"" + pid + "\"]");

        String signature = generateSignature(params, clientSecret);
        params.put("sign", signature);

        log.info("请求参数：");
        params.forEach((key, value) -> log.info("  {} = {}", key, value));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = createRequestEntity(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(PDD_API_URL, request, String.class);

        log.info("生成授权链接响应状态码：{}", response.getStatusCode());
        log.info("生成授权链接响应内容：{}", response.getBody());

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new Exception("API 返回空响应");
        }

        JsonNode rootNode = objectMapper.readTree(response.getBody());

        if (rootNode.has("error_response")) {
            JsonNode errorNode = rootNode.get("error_response");
            String errorMsg = errorNode.has("sub_msg") ? errorNode.get("sub_msg").asText() : "未知错误";
            throw new Exception("生成授权链接失败：" + errorMsg);
        }

        String authUrl = rootNode.path("rp_prom_url_generate_response").path("url").asText();
        log.info("生成授权链接成功：{}", authUrl);

        return authUrl;
    }

    /**
     * 查询授权状态
     */
    public boolean checkAuthority(String pid) throws Exception {
        log.info("开始查询授权状态，PID: {}", pid);

        Map<String, Object> params = new HashMap<>();
        params.put("type", "pdd.ddk.member.authority.query");
        params.put("client_id", clientId);
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("pid", pid);

        String signature = generateSignature(params, clientSecret);
        params.put("sign", signature);

        log.info("请求参数：");
        params.forEach((key, value) -> log.info("  {} = {}", key, value));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = createRequestEntity(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(PDD_API_URL, request, String.class);

        log.info("查询授权状态响应：{}", response.getBody());

        if (response.getBody() == null || response.getBody().isEmpty()) {
            log.warn("API 返回空响应");
            return false;
        }

        JsonNode rootNode = objectMapper.readTree(response.getBody());

        if (rootNode.has("error_response")) {
            JsonNode errorNode = rootNode.get("error_response");
            String errorMsg = errorNode.has("sub_msg") ? errorNode.get("sub_msg").asText() : "未知错误";
            log.error("API 返回错误：{}", errorMsg);
            return false;
        }

        JsonNode authorityNode = rootNode.path("authority_query_response");
        log.info("authority_query_response 节点：{}", authorityNode);

        boolean authorized = authorityNode.path("authority").asBoolean(false);
        if (!authorized) {
            authorized = authorityNode.path("bind").asInt(0) == 1;
        }
        log.info("授权状态：{}，authority={}, bind={}", authorized ? "已授权" : "未授权",
                authorityNode.path("authority"), authorityNode.path("bind"));

        return authorized;
    }

    /**
     * 生成 API 签名
     */
    private String generateSignature(Map<String, Object> params, String secret) throws Exception {
        StringBuilder sb = new StringBuilder();

        params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    sb.append(entry.getKey()).append(entry.getValue());
                });

        String signSource = secret + sb.toString() + secret;

        log.debug("签名原文：{}", signSource);

        MessageDigest md = MessageDigest.getInstance("MD5");
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

    /**
     * 创建请求实体
     */
    private HttpEntity<MultiValueMap<String, String>> createRequestEntity(
            Map<String, Object> params, HttpHeaders headers) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        params.forEach((key, value) -> formData.add(key, String.valueOf(value)));
        return new HttpEntity<>(formData, headers);
    }
}
