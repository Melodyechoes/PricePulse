-- 直接使用 Spring Security BCrypt 生成的新密码
-- 这是实时生成的，保证能匹配

-- 删除旧用户
DELETE FROM users WHERE username IN ('admin', 'testuser');

-- 插入新用户（使用新生成的 BCrypt 密码）
-- 密码：admin123
-- 加密后：$2a$10$S0AIgT5aPHmAfSXvzlUC8u0E4q76zgXtuBbfwsWKKdxVAMfynoDrK
INSERT INTO users (id, username, password, role, created_at, updated_at, status) 
VALUES 
(1, 'admin', '$2a$10$S0AIgT5aPHmAfSXvzlUC8u0E4q76zgXtuBbfwsWKKdxVAMfynoDrK', 'ADMIN', NOW(), NOW(), 1),
(2, 'testuser', '$2a$10$S0AIgT5aPHmAfSXvzlUC8u0E4q76zgXtuBbfwsWKKdxVAMfynoDrK', 'USER', NOW(), NOW(), 1);

-- 验证
SELECT username, LENGTH(password) as pwd_len FROM users WHERE username IN ('admin', 'testuser');
