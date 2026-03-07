package com.pricepulse.backend.controller;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取用户的通知列表
     */
    @GetMapping
    public Result<List<com.pricepulse.backend.common.entity.NotificationEntity>> getUserNotifications(
            @RequestParam Long userId) {
        try {
            List<com.pricepulse.backend.common.entity.NotificationEntity> notifications =
                    notificationService.getUserNotifications(userId);
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return Result.success();
        } catch (Exception e) {
            log.error("标记通知失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/mark-all-read")
    public Result<Void> markAllAsRead(@RequestParam Long userId) {
        try {
            notificationService.markAllAsRead(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("批量标记已读失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(@RequestParam Long userId) {
        try {
            Map<String, Integer> result = new HashMap<>();
            result.put("count", notificationService.getUnreadCount(userId));
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取未读数量失败", e);
            return Result.error(e.getMessage());
        }
    }
}
