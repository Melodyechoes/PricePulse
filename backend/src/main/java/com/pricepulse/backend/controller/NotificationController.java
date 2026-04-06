package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.entity.NotificationEntity;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知管理控制器
 * <p>
 * 提供通知列表查询、标记已读、删除通知等功能
 *
 * @author PricePulse Team
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取用户的通知列表（分页）
     *
     * @param userId 用户ID，默认1
     * @param page 页码，默认1
     * @param pageSize 每页数量，默认20
     * @return 分页通知列表，包含list、total、page、pageSize字段
     */
    @GetMapping
    public Result getUserNotifications(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<NotificationEntity> notifications = notificationService.getUserNotifications(userId);

            // 简单分页
            int total = notifications.size();
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, total);

            if (fromIndex >= total) {
                return Result.success(new HashMap<String, Object>() {{
                    put("list", new java.util.ArrayList<>());
                    put("total", 0);
                    put("page", page);
                    put("pageSize", pageSize);
                }});
            }

            List<NotificationEntity> pageData = notifications.subList(fromIndex, toIndex);

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageData);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取通知列表失败, userId={}, page={}", userId, page, e);
            return Result.error(e.getMessage());
        }
    }
    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return Result.success();
        } catch (Exception e) {
            log.error("标记通知已读失败, id={}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除通知失败, id={}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 全部标记已读
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/mark-all-read")
    public Result<Void> markAllAsRead(@RequestParam Long userId) {
        try {
            notificationService.markAllAsRead(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("批量标记已读失败, userId={}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量Map，格式: {"count": 5}
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(@RequestParam Long userId) {
        try {
            Map<String, Integer> result = new HashMap<>();
            result.put("count", notificationService.getUnreadCount(userId));
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取未读数量失败, userId={}", userId, e);
            return Result.error(e.getMessage());
        }
    }
}
