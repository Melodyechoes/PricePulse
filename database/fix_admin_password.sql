-- 验证 admin 用户密码
SELECT id, username, role, LENGTH(password) as pwd_len FROM users WHERE username='admin';

-- 重新设置正确的 BCrypt 密码 (admin123)
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';

-- 再次验证
SELECT id, username, role, SUBSTRING(password, 1, 60) as pwd_preview FROM users WHERE username='admin';
