package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.dto.LoginDTO;
import com.pricepulse.backend.common.dto.RegisterDTO;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto.getUsername(), dto.getPassword());
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<?> login(HttpServletRequest request,
                           @RequestBody LoginDTO dto) {
        authService.login(request, dto.getUsername(), dto.getPassword());
        return Result.success("登录成功");
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        session.invalidate(); // 删除 Redis 中的 Session
        return Result.success("退出成功");
    }
}
