-- 生成新的 admin123 BCrypt 密码并更新
-- 这是使用 Spring Security BCrypt 生成的标准密码

UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';

UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'testuser';

-- 验证更新
SELECT id, username, role, status, SUBSTRING(password, 1, 60) as pwd_preview FROM users;
