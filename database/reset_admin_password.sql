-- 删除 admin 和 testuser 用户
DELETE FROM users WHERE username IN ('admin', 'testuser');

-- 重新插入 admin 用户（密码：admin123）
-- BCrypt 加密后的密码：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (id, username, password, role, created_at, updated_at, status) 
VALUES 
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW(), NOW(), 1),
(2, 'testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', NOW(), NOW(), 1);

-- 验证插入结果
SELECT id, username, role, status, SUBSTRING(password, 1, 60) as pwd_preview, LENGTH(password) as pwd_len 
FROM users 
WHERE username IN ('admin', 'testuser');
