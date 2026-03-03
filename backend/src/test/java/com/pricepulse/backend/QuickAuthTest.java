package com.pricepulse.backend;

import com.pricepulse.backend.common.dto.LoginRequest;
import com.pricepulse.backend.common.dto.RegisterRequest;
import com.pricepulse.backend.common.util.JwtUtil;
import com.pricepulse.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuickAuthTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testRegisterSuccess() {
        System.out.println("🚀 开始用户注册测试...");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser_" + (System.currentTimeMillis() % 1000000));
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        Map<String, Object> response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.get("token")).isNotNull();
        assertThat(response.get("userInfo")).isNotNull();

        System.out.println("✅ 用户注册成功，Token: " + response.get("token"));
    }

    @Test
    void testLoginSuccess() {
        System.out.println("🚀 开始用户登录测试...");

        // 先注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        String username = "login_" + (System.currentTimeMillis() % 1000000);
        registerRequest.setUsername(username);
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        authService.register(registerRequest);

        // 测试登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("password123");

        Map<String, Object> response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.get("token")).isNotNull();
        assertThat(response.get("userInfo")).isNotNull();

        System.out.println("✅ 用户登录成功，Token: " + response.get("token"));
    }

    @Test
    void testJwtUtil() {
        System.out.println("🚀 开始JWT工具测试...");

        Long userId = 1L;
        String username = "jwtTestUser";

        // 生成Token
        String token = jwtUtil.generateToken(userId, username);

        assertThat(token).isNotNull();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo(username);

        System.out.println("✅ JWT工具测试通过");
    }
}
