package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.entity.NotificationEntity;
import com.pricepulse.backend.common.websocket.NotificationWebSocketHandler;
import com.pricepulse.backend.mapper.NotificationMapper;
import com.pricepulse.backend.service.crawler.CrawlerStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 测试 WebSocket 推送
     */
    @GetMapping("/ws/push")
    public String testWebSocketPush(@RequestParam Long userId) {
        try {
            String message = "{\"type\":\"PRICE_DROP\",\"message\":\"📉 这是测试通知！商品价格已更新。\",\"time\":\"" + LocalDateTime.now() + "\"}";

            webSocketHandler.sendNotification(userId, message);

            return "已向用户 " + userId + " 发送测试通知";
        } catch (Exception e) {
            return "发送失败：" + e.getMessage();
        }
    }

    /**
     * 检查 WebSocket 连接状态
     */
    @GetMapping("/ws/status")
    public String getWebSocketStatus() {
        return "WebSocket 服务运行中";
    }

    /**
     * 创建测试通知数据
     */
    @GetMapping("/notification/create")
    public String createTestNotification(@RequestParam(defaultValue = "1") Long userId) {
        try {
            NotificationEntity notification = new NotificationEntity();
            notification.setUserId(userId);
            notification.setMessage("📉 您关注的商品【测试商品】降价了！原价 ¥9999.00，现价 ¥8999.00，降幅 10.0%");
            notification.setType("PRICE_DROP");
            notification.setIsRead(0);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());

            notificationMapper.insert(notification);

            // 同时发送 WebSocket 通知
            String wsMessage = "{\"type\":\"PRICE_DROP\",\"message\":\"📉 您有新的降价通知，快来查看吧！\",\"timestamp\":" + System.currentTimeMillis() + "}";
            webSocketHandler.sendNotification(userId, wsMessage);

            return "✅ 已为用户 " + userId + " 创建测试通知数据";
        } catch (Exception e) {
            return "❌ 创建失败：" + e.getMessage();
        }
    }

    /**
     * 批量创建测试通知数据
     */
    @GetMapping("/notification/batch")
    public String batchCreateTestNotifications(@RequestParam(defaultValue = "1") Long userId) {
        try {
            String[] messages = {
                    "📉 您关注的商品【Apple iPhone 15 Pro】降价了！原价 ¥8999.00，现价 ¥7999.00，降幅 11.1%",
                    "📉 您关注的商品【华为 Mate 60 Pro】降价了！原价 ¥6999.00，现价 ¥6499.00，降幅 7.1%",
                    "📉 您关注的商品【小米 14 Ultra】降价了！原价 ¥5999.00，现价 ¥5499.00，降幅 8.3%",
                    "📉 您关注的商品【OPPO Find X7】降价了！原价 ¥4999.00，现价 ¥4599.00，降幅 8.0%",
                    "📉 您关注的商品【vivo X100 Pro】降价了！原价 ¥4999.00，现价 ¥4699.00，降幅 6.0%"
            };

            for (int i = 0; i < messages.length; i++) {
                NotificationEntity notification = new NotificationEntity();
                notification.setUserId(userId);
                notification.setMessage(messages[i]);
                notification.setType("PRICE_DROP");
                notification.setIsRead(i == 0 ? 0 : 1);
                notification.setCreatedAt(LocalDateTime.now().minusMinutes(i * 10));
                notification.setUpdatedAt(LocalDateTime.now());

                notificationMapper.insert(notification);
            }

            // 发送 WebSocket 通知
            String wsMessage = "{\"type\":\"PRICE_DROP\",\"message\":\"📢 您有 5 条新的价格通知！\",\"timestamp\":" + System.currentTimeMillis() + "}";
            webSocketHandler.sendNotification(userId, wsMessage);

            return "✅ 已批量创建 " + messages.length + " 条测试通知数据";
        } catch (Exception e) {
            return "❌ 批量创建失败：" + e.getMessage();
        }
    }

    @Autowired
    private CrawlerStrategyFactory crawlerStrategyFactory;

    @GetMapping("/pdd/auth-url")
    public ResponseEntity<?> generatePddAuthUrl() {
        try {
            // 这里需要获取 PddApiServiceImpl 实例
            // 可以通过 Spring 注入或者直接调用方法
            Map<String, String> result = new HashMap<>();
            result.put("message", "请访问以下链接完成授权备案：");
            result.put("authUrl", "请在拼多多开放平台后台生成授权链接");
            result.put("note", "生成后访问该链接完成备案，然后调用 /pdd/check-auth 检查是否成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("生成失败：" + e.getMessage());
        }
    }

}
