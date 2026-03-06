package com.pricepulse.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NotificationService {

    // 内存存储通知
    private final Map<Long, List<Notification>> userNotifications = new ConcurrentHashMap<>();

    /**
     * 发送降价通知
     */
    public void sendPriceDropNotification(Long userId, String productName,
                                          Double oldPrice, Double newPrice) {
        double dropPercent = ((oldPrice - newPrice) / oldPrice) * 100;

        String message = String.format("📉 您关注的商品【%s】降价了！原价 ¥%.2f，现价 ¥%.2f，降幅 %.1f%%",
                productName, oldPrice, newPrice, dropPercent);

        log.info(message);

        // 保存通知
        saveNotification(userId, message, "PRICE_DROP");

        // TODO: 发送邮件通知
        // sendEmailNotification(userId, message);
    }

    /**
     * 发送到货通知
     */
    public void sendStockNotification(Long userId, String productName) {
        String message = "📦 您关注的商品【" + productName + "】已到货！";

        log.info(message);
        saveNotification(userId, message, "STOCK_IN");
    }

    /**
     * 获取用户的通知列表
     */
    public List<Notification> getUserNotifications(Long userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long notificationId) {
        // TODO: 实现标记已读逻辑
    }

    /**
     * 保存通知
     */
    private void saveNotification(Long userId, String message, String type) {
        Notification notification = new Notification(
                System.currentTimeMillis(),
                userId,
                message,
                type,
                false,
                LocalDateTime.now()
        );

        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, notification);

        // 限制每个用户最多保存 100 条通知
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications.size() > 100) {
            notifications.remove(notifications.size() - 1);
        }
    }

    /**
     * 通知实体类
     */
    public static class Notification {
        private Long id;
        private Long userId;
        private String message;
        private String type; // PRICE_DROP, STOCK_IN, etc.
        private Boolean isRead;
        private LocalDateTime createdAt;

        public Notification(Long id, Long userId, String message, String type,
                            Boolean isRead, LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.message = message;
            this.type = type;
            this.isRead = isRead;
            this.createdAt = createdAt;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
