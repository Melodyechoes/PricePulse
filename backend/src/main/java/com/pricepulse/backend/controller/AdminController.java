package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.AuthService;
import com.pricepulse.backend.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员接口
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 检查当前用户是否为管理员
     */
    @GetMapping("/check-admin")
    public Result<Map<String, Object>> checkAdmin(@RequestParam Long userId) {
        try {
            boolean isAdmin = authService.isAdmin(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isAdmin", isAdmin);
            
            if (isAdmin) {
                return Result.success(response);
            } else {
                // 返回错误时，手动设置 code 和 data
                Result<Map<String, Object>> result = new Result<>();
                result.setCode(403);
                result.setMessage("无管理员权限");
                result.setData(response);
                return result;
            }
        } catch (Exception e) {
            log.error("检查管理员权限失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 验证管理员权限（拦截器）
     */
    @GetMapping("/verify")
    public Result<Void> verifyAdmin(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未提供认证令牌");
            }

            // 提取 token（去掉 "Bearer " 前缀）
            String jwtToken = token.substring(7);
            
            // 验证 token 是否有效
            if (!jwtUtil.validateToken(jwtToken)) {
                return Result.error("无效的认证令牌或令牌已过期");
            }
            
            // 从 token 中获取用户 ID
            Long userId = jwtUtil.getUserIdFromToken(jwtToken);

            if (!authService.isAdmin(userId)) {
                return Result.error("无管理员权限");
            }

            return Result.success();
        } catch (Exception e) {
            log.error("验证管理员权限失败", e);
            return Result.error(e.getMessage());
        }
    }
}
