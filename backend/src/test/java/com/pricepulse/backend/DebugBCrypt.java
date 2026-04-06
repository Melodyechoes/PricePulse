package com.pricepulse.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DebugBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "admin123";
        String dbPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        System.out.println("========== BCrypt 调试信息 ==========");
        System.out.println("原始密码：" + rawPassword);
        System.out.println("数据库密码：" + dbPassword);
        System.out.println("数据库密码长度：" + dbPassword.length());
        System.out.println("匹配结果：" + encoder.matches(rawPassword, dbPassword));
        
        // 生成新密码测试
        String newPassword = encoder.encode(rawPassword);
        System.out.println("\n新生成的密码：" + newPassword);
        System.out.println("新密码长度：" + newPassword.length());
        System.out.println("新密码匹配：" + encoder.matches(rawPassword, newPassword));
        System.out.println("旧密码能否匹配新密码：" + encoder.matches(rawPassword, dbPassword));
    }
}
