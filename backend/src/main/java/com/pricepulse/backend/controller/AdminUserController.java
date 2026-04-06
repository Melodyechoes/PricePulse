package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.entity.User;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.common.util.JwtUtil;
import com.pricepulse.backend.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员用户管理控制器
 * <p>
 * 提供用户列表查询、用户详情、状态管理、密码重置等管理员功能
 * 所有接口需要ADMIN角色权限
 *
 * @author PricePulse Team
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/api/admin/users")
@Slf4j
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 验证管理员权限
     * <p>
     * 从JWT Token中提取userId，验证用户是否具有ADMIN角色
     *
     * @param request HTTP请求对象，包含JWT解析后的userId
     * @throws RuntimeException 当用户未登录或非管理员时抛出
     */
    private void checkAdminPermission(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.debug("从 request 中获取 userId: {}", userId);
        
        if (userId == null) {
            log.error("userId 为空, JWT 拦截器可能未执行");
            throw new RuntimeException("未授权访问");
        }
        
        User user = adminUserService.getUserById(userId);
        log.debug("查询到用户: {}, 角色: {}", user != null ? user.getUsername() : "null", user != null ? user.getRole() : "null");
        
        if (user == null || !"ADMIN".equals(user.getRole())) {
            log.error("用户不是管理员, userId={}, role={}", userId, user != null ? user.getRole() : "null");
            throw new RuntimeException("无管理员权限");
        }
        
        log.info("管理员权限验证通过, userId={}, username={}", userId, user.getUsername());
    }

    /**
     * 获取用户列表（分页）
     *
     * @param page 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param request HTTP请求对象，用于权限验证
     * @return 分页用户列表，包含total、list、page、pageSize字段
     */
    @GetMapping
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Map<String, Object> result = adminUserService.getAllUsers(page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败, page={}, pageSize={}", page, pageSize, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @param request HTTP请求对象，用于权限验证
     * @return 用户详细信息（不包含密码）
     */
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            User user = adminUserService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户详情失败, id={}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id 用户ID
     * @param status 状态值，1-启用，0-禁用
     * @param request HTTP请求对象，用于权限验证
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            adminUserService.updateUserStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新用户状态失败, id={}, status={}", id, status, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     * @param request HTTP请求对象，用于权限验证
     * @return 操作结果
     */
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            adminUserService.resetPassword(id, newPassword);
            return Result.success();
        } catch (Exception e) {
            log.error("重置用户密码失败, id={}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @param request HTTP请求对象，用于权限验证
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            adminUserService.deleteUser(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除用户失败, id={}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     * <p>
     * 统计总用户数、活跃用户数、管理员数、普通用户数等
     *
     * @param request HTTP请求对象，用于权限验证
     * @return 统计数据Map
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Map<String, Object> stats = adminUserService.getUserStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}
