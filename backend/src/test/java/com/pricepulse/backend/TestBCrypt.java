package com.pricepulse.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试 admin123 的密码
        String rawPassword = "admin123";
        String encodedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        System.out.println("原始密码：" + rawPassword);
        System.out.println("数据库密码：" + encodedPassword);
        System.out.println("是否匹配：" + encoder.matches(rawPassword, encodedPassword));
        
        // 重新生成一个密码
        String newEncoded = encoder.encode(rawPassword);
        System.out.println("\n新生成的密码：" + newEncoded);
        System.out.println("新密码是否匹配：" + encoder.matches(rawPassword, newEncoded));
    }
}
