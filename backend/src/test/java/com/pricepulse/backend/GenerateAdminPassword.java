package com.pricepulse.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateAdminPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 admin123 的 BCrypt 密码
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("========== 管理员密码信息 ==========");
        System.out.println("原始密码：" + rawPassword);
        System.out.println("BCrypt 加密后：" + encodedPassword);
        System.out.println("\n========== SQL 更新语句 ==========");
        System.out.println("UPDATE users SET password = '" + encodedPassword + "' WHERE username = 'admin';");
        System.out.println("\n========== 验证密码 ==========");
        System.out.println("验证匹配：" + encoder.matches(rawPassword, encodedPassword));
    }
}
