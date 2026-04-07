package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.dto.ChangePasswordRequest;
import com.pricepulse.backend.common.dto.LoginRequest;
import com.pricepulse.backend.common.dto.RegisterRequest;
import com.pricepulse.backend.common.dto.UserInfo;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 * <p>
 * 提供用户注册、登录、获取用户信息等认证相关接口
 *
 * @author PricePulse Team
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户注册
     *
     * @param request 注册请求参数，包含用户名、密码、确认密码
     * @return 注册结果，包含用户信息和JWT Token
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = authService.register(request);
            return Result.success("注册成功", response);
        } catch (Exception e) {
            log.error("用户注册失败, username: {}", request.getUsername(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param request 登录请求参数，包含用户名、密码
     * @return 登录结果，包含用户信息和JWT Token
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Map<String, Object> response = authService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("用户登录失败, username: {}", request.getUsername(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/me")
    public Result<UserInfo> getCurrentUser(@RequestParam Long userId) {
        try {
            UserInfo userInfo = authService.getCurrentUserInfo(userId);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败, userId: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求，包含旧密码和新密码
     * @param httpRequest HTTP请求对象，用于从JWT中提取userId
     * @return 操作结果
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录或Token无效");
            }
            authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return Result.success();
        } catch (Exception e) {
            log.error("修改密码失败, userId: {}", httpRequest.getAttribute("userId"), e);
            return Result.error(e.getMessage());
        }
    }
}
