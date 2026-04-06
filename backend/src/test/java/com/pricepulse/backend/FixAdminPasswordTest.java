package com.pricepulse.backend;

import com.pricepulse.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 修复管理员密码测试
 */
@SpringBootTest
public class FixAdminPasswordTest {

    @Autowired
    private UserMapper userMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 修复管理员密码为 admin123
     */
    @Test
    public void fixAdminPassword() {
        // admin123 的 BCrypt 加密
        String encodedPassword = passwordEncoder.encode("admin123");
        System.out.println("加密后的密码：" + encodedPassword);
        
        // 更新 admin 用户的密码
        int result = userMapper.updatePassword("admin", encodedPassword);
        
        if (result > 0) {
            System.out.println("管理员密码已成功更新为：admin123");
        } else {
            System.out.println("密码更新失败");
        }
    }
}
