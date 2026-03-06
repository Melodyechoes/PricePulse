package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<List<NotificationService.Notification>> getUserNotifications(
            @RequestParam Long userId) {
        try {
            List<NotificationService.Notification> notifications =
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
}
