package com.pricepulse.backend.controller;

import com.pricepulse.backend.service.PddAuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pdd")
@Slf4j
public class PddAuthorityController {

    @Autowired
    private PddAuthorityService pddAuthorityService;

    @Value("${crawler.pdd.api.client-id}")
    private String clientId;

    @Value("${crawler.pdd.api.client-secret}")
    private String clientSecret;

    @Value("${crawler.pdd.api.pid:}")
    private String pid;

    @GetMapping("/auth-url")
    public ResponseEntity<?> generateAuthUrl() {
        log.info("收到生成授权链接请求");

        try {
            pddAuthorityService.setCredentials(clientId, clientSecret);

            if (pid == null || pid.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "PID 未配置，请在 application.yml 中配置 crawler.pdd.api.pid");
                return ResponseEntity.badRequest().body(error);
            }

            String authUrl = pddAuthorityService.generateAuthorityUrl(pid);

            Map<String, String> result = new HashMap<>();
            result.put("success", "true");
            result.put("message", "授权链接生成成功");
            result.put("authUrl", authUrl);
            result.put("pid", pid);
            result.put("instruction", "请复制以上链接到浏览器打开，完成授权备案流程");

            log.info("授权链接生成成功：{}", authUrl);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("生成授权链接失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "生成失败：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuthority() {
        log.info("收到检查授权状态请求");

        try {
            pddAuthorityService.setCredentials(clientId, clientSecret);

            if (pid == null || pid.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "PID 未配置");
                return ResponseEntity.badRequest().body(error);
            }

            log.info("检查授权状态，使用的 PID: {}", pid);

            boolean authorized = pddAuthorityService.checkAuthority(pid);

            Map<String, Object> result = new HashMap<>();
            result.put("success", "true");
            result.put("authorized", authorized);
            result.put("pid", pid);

            if (authorized) {
                result.put("message", "该 PID 已完成授权备案，可以正常调用 API");
            } else {
                result.put("message", "该 PID 未完成授权备案，请先调用 /api/pdd/auth-url 生成授权链接并完成备案");
                result.put("nextStep", "访问 /api/pdd/auth-url 获取授权链接");
            }

            log.info("授权状态检查结果：{}", authorized ? "已授权" : "未授权");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("检查授权状态失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "检查失败：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/test-api")
    public ResponseEntity<?> testApi() {
        log.info("收到测试 API 请求");

        Map<String, Object> result = new HashMap<>();
        result.put("message", "拼多多 API 授权测试接口");
        result.put("clientId", clientId.substring(0, 8) + "...");
        result.put("pid", pid);
        result.put("configured", pid != null && !pid.isEmpty());

        return ResponseEntity.ok(result);
    }
}
