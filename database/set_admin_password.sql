-- 设置管理员账户密码
-- 用户名：admin
-- 密码：admin123
-- 注意：这是 BCrypt 加密后的密码

UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin' AND role = 'ADMIN';

-- 如果需要创建新的管理员账户，使用以下 SQL：
-- INSERT INTO users (username, password, role, status) 
-- VALUES ('administrator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1);
