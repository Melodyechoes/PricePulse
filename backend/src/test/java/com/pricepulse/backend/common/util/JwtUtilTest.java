package com.pricepulse.backend.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateAndValidateToken() {
        Long userId = 1L;
        String username = "testUser";

        // 生成Token
        String token = jwtUtil.generateToken(userId, username);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();

        // 验证Token
        boolean isValid = jwtUtil.validateToken(token);
        assertThat(isValid).isTrue();

        // 获取用户ID
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);
        assertThat(extractedUserId).isEqualTo(userId);

        // 获取用户名
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtil.validateToken(invalidToken);
        assertThat(isValid).isFalse();
    }
}
