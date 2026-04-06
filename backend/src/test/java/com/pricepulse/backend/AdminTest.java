package com.pricepulse.backend;

import com.pricepulse.backend.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 管理员功能测试
 */
@SpringBootTest
@Slf4j
public class AdminTest {

    @Autowired
    private AuthService authService;

    /**
     * 测试检查管理员权限
     */
    @Test
    public void testCheckAdmin() {
        log.info("=== 测试管理员权限检查 ===");
        
        // 测试 admin 用户（ID=1，应该是 ADMIN 角色）
        Long adminUserId = 1L;
        boolean isAdmin = authService.isAdmin(adminUserId);
        
        log.info("用户 ID {} 是否为管理员：{}", adminUserId, isAdmin);
        
        if (isAdmin) {
            log.info("✅ admin 用户是管理员");
        } else {
            log.warn("❌ admin 用户不是管理员，请检查数据库 role 字段");
        }
        
        assertThat(isAdmin).isTrue();
        
        // 测试普通用户（ID=2，应该是 USER 角色）
        Long normalUserId = 2L;
        boolean isNormalUserAdmin = authService.isAdmin(normalUserId);
        
        log.info("用户 ID {} 是否为管理员：{}", normalUserId, isNormalUserAdmin);
        
        if (!isNormalUserAdmin) {
            log.info("✅ testuser 用户是普通用户");
        } else {
            log.warn("❌ testuser 用户被错误设置为管理员");
        }
        
        assertThat(isNormalUserAdmin).isFalse();
    }

    /**
     * 测试不存在的用户
     */
    @Test
    public void testCheckNonExistentUser() {
        log.info("=== 测试不存在的用户 ===");
        
        Long nonExistentUserId = 99999L;
        boolean isAdmin = authService.isAdmin(nonExistentUserId);
        
        log.info("不存在的用户 ID {} 是否为管理员：{}", nonExistentUserId, isAdmin);
        assertThat(isAdmin).isFalse();
    }
}
