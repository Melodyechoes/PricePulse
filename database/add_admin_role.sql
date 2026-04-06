-- 为 users 表添加 role 字段
ALTER TABLE `users` 
ADD COLUMN `role` varchar(20) DEFAULT 'USER' COMMENT '用户角色：ADMIN=管理员，USER=普通用户' 
AFTER `password`;

-- 将 admin 用户的角色设置为 ADMIN
UPDATE `users` SET `role` = 'ADMIN' WHERE `username` = 'admin';

-- 确保其他用户的角色为 USER
UPDATE `users` SET `role` = 'USER' WHERE `username` != 'admin' AND (`role` IS NULL OR `role` = '');
