-- 使用新生成的 BCrypt 密码
-- 密码：admin123
-- 加密后：$2a$10$S0AIgT5aPHmAfSXvzlUC8u0E4q76zgXtuBbfwsWKKdxVAMfynoDrK

UPDATE users 
SET password = '$2a$10$S0AIgT5aPHmAfSXvzlUC8u0E4q76zgXtuBbfwsWKKdxVAMfynoDrK' 
WHERE username = 'admin';

UPDATE users 
SET password = '$2a$10$S0AIgT5aPHmAfSXvzlUC8u0E4q76zgXtuBbfwsWKKdxVAMfynoDrK' 
WHERE username = 'testuser';

SELECT username, password, LENGTH(password) as len FROM users WHERE username IN ('admin', 'testuser');
