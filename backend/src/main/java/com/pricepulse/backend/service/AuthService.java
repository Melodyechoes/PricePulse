package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.User;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * 登录注册
 */

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    public void register(String username, String password) {
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }
        // 密码加密（务必做！）
        String encodedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        userMapper.save(user);
    }

    public void login(HttpServletRequest request, String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 登录成功，保存用户到 Session
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
    }
}