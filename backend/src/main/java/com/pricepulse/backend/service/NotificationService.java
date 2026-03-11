package com.pricepulse.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricepulse.backend.common.entity.NotificationEntity;
import com.pricepulse.backend.common.websocket.NotificationWebSocketHandler;
import com.pricepulse.backend.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 发送降价通知
     */
    public void sendPriceDropNotification(Long userId, String productName,
                                          BigDecimal oldPrice, BigDecimal newPrice) {
        double dropPercent = ((oldPrice.doubleValue() - newPrice.doubleValue()) / oldPrice.doubleValue()) * 100;

        String message = String.format("📉 您关注的商品【%s】降价了！原价 ¥%.2f，现价 ¥%.2f，降幅 %.1f%%",
                productName, oldPrice.doubleValue(), newPrice.doubleValue(), dropPercent);

        log.info(message);

        // 保存通知到数据库
        saveNotification(userId, message, "PRICE_DROP");

        // 发送 WebSocket 实时通知
        sendWebSocketNotification(userId, "PRICE_DROP", message);
    }
    /**
     * 获取用户的通知列表
     */
    public List<NotificationEntity> getUserNotifications(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long notificationId) {
        notificationMapper.updateIsRead(notificationId, 1);
        log.info("标记通知 {} 为已读", notificationId);
    }

    /**
     * 删除通知
     */
    public void deleteNotification(Long notificationId) {
        notificationMapper.deleteById(notificationId);
        log.info("删除通知 {}", notificationId);
    }

    /**
     * 批量标记已读
     */
    public void markAllAsRead(Long userId) {
        notificationMapper.updateAllAsRead(userId);
        log.info("用户 {} 的所有通知标记为已读", userId);
    }

    /**
     * 获取未读通知数量
     */
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    /**
     * 保存通知
     */
    private void saveNotification(Long userId, String message, String type) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(0); // 默认为未读
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("保存通知：userId={}, message={}, type={}", userId, message, type);
    }

    /**
     * 发送 WebSocket 实时通知
     */
    private void sendWebSocketNotification(Long userId, String type, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notification);
            webSocketHandler.sendNotification(userId, jsonMessage);

            log.debug("已发送 WebSocket 通知给 userId={}", userId);
        } catch (Exception e) {
            log.error("发送 WebSocket 通知失败", e);
        }
    }
}
