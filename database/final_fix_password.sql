-- 使用 Spring Security BCrypt 生成的正确密码
-- 原始密码：admin123
-- 加密密码：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- 更新 admin 用户密码
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';

-- 更新 testuser 用户密码
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'testuser';

-- 验证更新结果
SELECT id, username, role, status, LENGTH(password) as pwd_len FROM users;
