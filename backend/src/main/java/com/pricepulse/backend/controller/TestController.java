package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*") // 允许跨域
public class TestController {

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

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
}
