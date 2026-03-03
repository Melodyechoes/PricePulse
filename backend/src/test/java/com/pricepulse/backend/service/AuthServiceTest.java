package com.pricepulse.backend.service;

import com.pricepulse.backend.common.dto.LoginRequest;
import com.pricepulse.backend.common.dto.RegisterRequest;
import com.pricepulse.backend.common.dto.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser_" + System.currentTimeMillis());
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        Map<String, Object> response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.get("token")).isNotNull();
        assertThat(response.get("userInfo")).isNotNull();
    }

    @Test
    void testRegisterDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicateUser");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        // 第一次注册成功
        authService.register(request);

        // 第二次注册应该失败
        assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });
    }

    @Test
    void testLoginSuccess() {
        // 先注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        String username = "loginTestUser_" + System.currentTimeMillis();
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
    }

    @Test
    void testLoginWithWrongPassword() {
        // 先注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        String username = "wrongPwdUser_" + System.currentTimeMillis();
        registerRequest.setUsername(username);
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        authService.register(registerRequest);

        // 使用错误密码登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("wrongPassword");

        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void testGetCurrentUserInfo() {
        // 先注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        String username = "getCurrentUser_" + System.currentTimeMillis();
        registerRequest.setUsername(username);
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        Map<String, Object> registerResponse = authService.register(registerRequest);
        UserInfo userInfo = (UserInfo) registerResponse.get("userInfo");

        // 获取用户信息
        UserInfo currentUserInfo = authService.getCurrentUserInfo(userInfo.getId());

        assertThat(currentUserInfo).isNotNull();
        assertThat(currentUserInfo.getUsername()).isEqualTo(username);
    }
}
