package com.pricepulse.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricepulse.backend.common.dto.LoginRequest;
import com.pricepulse.backend.common.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        // 使用短用户名，确保不超过20个字符
        String username = "test_" + (System.currentTimeMillis() % 100000);
        request.setUsername(username);
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userInfo.username").value(username));
    }

    @Test
    void testRegisterWithDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        String username = "duplicate_" + (System.currentTimeMillis() % 100000);
        request.setUsername(username);
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        String requestJson = objectMapper.writeValueAsString(request);

        // 第一次注册成功
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 第二次注册应该失败
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(result -> {
                    // 期望返回错误信息（code=400 或其他非200）
                    String response = result.getResponse().getContentAsString();
                    org.junit.jupiter.api.Assertions.assertTrue(
                        response.contains("\"code\":400") || response.contains("用户名已存在"),
                        "重复注册应该失败"
                    );
                });
    }


    @Test
    void testLogin() throws Exception {
        // 先注册用户（使用更短的用户名）
        RegisterRequest registerRequest = new RegisterRequest();
        String username = "login_" + System.currentTimeMillis() % 1000000; // 限制在20字符内
        registerRequest.setUsername(username);
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 测试登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("password123");

        String requestJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.message").value("登录成功"));
    }


    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistentUser");
        request.setPassword("wrongPassword");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }


}
