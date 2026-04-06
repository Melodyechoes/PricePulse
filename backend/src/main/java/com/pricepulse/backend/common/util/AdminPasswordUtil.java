package com.pricepulse.backend.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 管理员密码加密工具
 * 用于生成 BCrypt 加密后的密码
 */
public class AdminPasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        // 生成默认管理员密码：admin123
        String adminPassword = "admin123";
        String encoded = encode(adminPassword);
        
        System.out.println("========== 管理员密码信息 ==========");
        System.out.println("用户名：admin");
        System.out.println("原始密码：" + adminPassword);
        System.out.println("加密密码：" + encoded);
        System.out.println("======================================");
        
        // 更新数据库的 SQL 语句
        System.out.println("\n========== 数据库更新 SQL ==========");
        System.out.println("UPDATE users SET password = '" + encoded + "' WHERE username = 'admin' AND role = 'ADMIN';");
        System.out.println("======================================\n");
        
        // 创建新的管理员账户示例
        String newPassword = "Admin@2026";
        String newEncoded = encode(newPassword);
        
        System.out.println("========== 新管理员账户示例 ==========");
        System.out.println("用户名：administrator");
        System.out.println("原始密码：" + newPassword);
        System.out.println("加密密码：" + newEncoded);
        System.out.println("\nSQL 插入语句:");
        System.out.println("INSERT INTO users (username, password, role, status) VALUES ('administrator', '" + 
                          newEncoded + "', 'ADMIN', 1);");
        System.out.println("======================================");
    }
}
