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
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
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

        log.info("用户注册成功，用户名：{}", request.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userInfo", convertToUserInfo(user));

        return response;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        log.info("用户登录成功，用户名：{}", request.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userInfo", convertToUserInfo(user));

        return response;
    }

    /**
     * 获取当前用户信息
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
     */
    private UserInfo convertToUserInfo(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
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

}
