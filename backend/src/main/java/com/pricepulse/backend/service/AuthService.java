package com.pricepulse.backend.service;

import com.pricepulse.backend.common.dto.LoginRequest;
import com.pricepulse.backend.common.dto.RegisterRequest;
import com.pricepulse.backend.common.dto.UserInfo;
import com.pricepulse.backend.common.entity.User;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.common.util.JwtUtil;
import com.pricepulse.backend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 * <p>
 * 提供用户注册、登录、权限验证等核心认证功能
 * 使用BCrypt加密密码，JWT生成Token
 *
 * @author PricePulse Team
 * @since 2026-04-06
 */
@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     * <p>
     * 1. 验证用户名长度（3-20字符）
     * 2. 验证密码长度（至少6字符）
     * 3. 验证两次密码一致性
     * 4. 检查用户名是否已存在
     * 5. BCrypt加密密码并保存
     * 6. 生成JWT Token
     *
     * @param request 注册请求参数
     * @return 包含Token和用户信息的Map
     * @throws BusinessException 当用户名已存在或参数校验失败时抛出
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        // 验证用户名长度
        if (request.getUsername() == null || request.getUsername().length() < 3 || request.getUsername().length() > 20) {
            throw new BusinessException("用户名长度必须在3-20个字符之间");
        }

        // 验证密码长度
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6个字符");
        }

        // 验证密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("注册失败");
        }

        log.info("用户注册成功, username: {}", request.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userInfo", convertToUserInfo(user));

        return response;
    }

    /**
     * 用户登录
     * <p>
     * 1. 查询用户信息
     * 2. 验证密码（BCrypt）
     * 3. 检查账号状态
     * 4. 生成JWT Token
     *
     * @param request 登录请求参数
     * @return 包含Token和用户信息的Map
     * @throws BusinessException 当用户名/密码错误或账号被禁用时抛出
     */
    public Map<String, Object> login(LoginRequest request) {
        log.debug("=== 登录请求 ===");
        log.debug("用户名: {}", request.getUsername());
        
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            log.warn("用户不存在: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        
        log.debug("查询到用户: {}", user.getUsername());
        
        // 验证密码
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.debug("密码匹配结果: {}", matches);
        
        if (!matches) {
            log.error("密码验证失败, username: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        log.info("用户登录成功, username: {}", request.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userInfo", convertToUserInfo(user));

        return response;
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户详细信息DTO
     * @throws BusinessException 当用户不存在时抛出
     */
    public UserInfo getCurrentUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserInfo(user);
    }

    /**
     * 转换为用户信息 DTO
     *
     * @param user 用户实体
     * @return 用户信息DTO
     */
    private UserInfo convertToUserInfo(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole() != null ? user.getRole() : "USER")
                .createdAt(user.getCreatedAt())
                .status(user.getStatus())
                .build();
    }

    /**
     * 用户注册
     */
    @Transactional
    public void register(String username, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setConfirmPassword(password);

        register(request);
    }

    /**
     * 用户登录
     */
    public void login(HttpServletRequest request, String username, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        login(loginRequest);
    }

    /**
     * 检查用户是否为管理员
     *
     * @param userId 用户ID
     * @return true-是管理员，false-非管理员
     */
    public boolean isAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && "ADMIN".equals(user.getRole());
    }

}
