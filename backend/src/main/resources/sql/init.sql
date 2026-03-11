-- Drop existing tables
DROP TABLE IF EXISTS user_products;
DROP TABLE IF EXISTS price_history;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       status TINYINT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create products table
CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          url TEXT NOT NULL,
                          image_url TEXT,
                          platform ENUM('TAOBAO', 'TMALL', 'JD', 'PDD', 'OTHER') NOT NULL,
                          platform_id VARCHAR(100),
                          brand VARCHAR(100),
                          category VARCHAR(100),
                          current_price DECIMAL(10,2),
                          original_price DECIMAL(10,2),
                          discount_rate DECIMAL(5,2),
                          sales_count INT DEFAULT 0,
                          rating DECIMAL(3,2),
                          review_count INT DEFAULT 0,
                          stock_status TINYINT DEFAULT 1,
                          last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          status TINYINT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create price history table
CREATE TABLE price_history (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               product_id BIGINT NOT NULL,
                               price DECIMAL(10,2) NOT NULL,
                               original_price DECIMAL(10,2),
                               discount_rate DECIMAL(5,2),
                               currency VARCHAR(10) DEFAULT 'CNY',
                               checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               source ENUM('AUTO', 'MANUAL') DEFAULT 'AUTO',
                               INDEX idx_product_id (product_id),
                               INDEX idx_checked_at (checked_at),
                               FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user products table
CREATE TABLE user_products (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               target_price DECIMAL(10,2),
                               notification_enabled TINYINT DEFAULT 1,
                               price_drop_threshold DECIMAL(5,2) DEFAULT 5.00,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               UNIQUE KEY uk_user_product (user_id, product_id),
                               INDEX idx_user_id (user_id),
                               INDEX idx_product_id (product_id),
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert test data
INSERT INTO users (username, password) VALUES
                                           ('admin', '$2a$10$r3dcP3.1zKzKzKzKzKzKzO.TEST.ENCRYPTED.PASSWORD'),
                                           ('testuser', '$2a$10$r3dcP3.1zKzKzKzKzKzKzO.TEST.ENCRYPTED.PASSWORD');

INSERT INTO products (name, url, image_url, platform, platform_id, brand, category, current_price, original_price) VALUES
                                                                                                                       ('iPhone 15 Pro Max', 'https://detail.tmall.com/item.htm?id=123456789', 'https://img.alicdn.com/imgextra/i1/123456789.jpg', 'TMALL', '123456789', 'Apple', '手机', 9999.00, 10999.00),
                                                                                                                       ('小米14 Ultra', 'https://item.jd.com/987654321.html', 'https://img10.360buyimg.com/n1/jfs/t1/123456789.jpg', 'JD', '987654321', '小米', '手机', 5999.00, 6999.00);

INSERT INTO price_history (product_id, price, original_price, checked_at) VALUES
                                                                              (1, 9999.00, 10999.00, NOW()),
                                                                              (2, 5999.00, 6999.00, NOW());

INSERT INTO user_products (user_id, product_id, target_price, price_drop_threshold) VALUES
                                                                                        (1, 1, 9500.00, 5.00),
                                                                                        (2, 2, 5500.00, 10.00);

-- Create indexes
CREATE INDEX idx_products_platform ON products(platform);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_price ON products(current_price);
CREATE INDEX idx_price_history_date_range ON price_history(checked_at, product_id);
CREATE INDEX idx_user_products_notification ON user_products(notification_enabled, user_id);



-- 添加 description 字段
ALTER TABLE products ADD COLUMN description TEXT COMMENT '商品描述';

-- Show tables
SHOW TABLES;


-- 创建通知表
CREATE TABLE IF NOT EXISTS notifications (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知 ID',
                                             user_id BIGINT NOT NULL COMMENT '用户 ID',
                                             message VARCHAR(500) NOT NULL COMMENT '通知内容',
                                             type VARCHAR(50) NOT NULL COMMENT '通知类型：PRICE_DROP-降价通知，STOCK_IN-到货通知',
                                             is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
                                             related_product_id BIGINT COMMENT '关联商品 ID',
                                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             INDEX idx_user_id (user_id),
                                             INDEX idx_type (type),
                                             INDEX idx_is_read (is_read),
                                             INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';

-- 插入测试数据（逐条插入，避免语法错误）
INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【iPhone 15 Pro Max】降价了！原价 9999.00 元，现价 9499.00 元，降幅 5.0%', 'PRICE_DROP', 0, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【Sony WH-1000XM5】降价了！原价 2499.00 元，现价 2299.00 元，降幅 8.0%', 'PRICE_DROP', 0, 26, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【iPad Pro 2024】已到货！', 'STOCK_IN', 1, 25, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【MacBook Pro 14】降价了！原价 12999.00 元，现价 11999.00 元，降幅 7.7%', 'PRICE_DROP', 1, 35, DATE_SUB(NOW(), INTERVAL 2 DAY));


USE price_pulse;

-- 创建通知表
CREATE TABLE IF NOT EXISTS notifications (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知 ID',
                                             user_id BIGINT NOT NULL COMMENT '用户 ID',
                                             message VARCHAR(500) NOT NULL COMMENT '通知内容',
                                             type VARCHAR(50) NOT NULL COMMENT '通知类型：PRICE_DROP-降价通知，STOCK_IN-到货通知',
                                             is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
                                             related_product_id BIGINT COMMENT '关联商品 ID',
                                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             INDEX idx_user_id (user_id),
                                             INDEX idx_type (type),
                                             INDEX idx_is_read (is_read),
                                             INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';

-- 插入测试数据
INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【iPhone 15 Pro Max】降价了！原价 9999.00 元，现价 9499.00 元，降幅 5.0%', 'PRICE_DROP', 0, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【Sony WH-1000XM5】降价了！原价 2499.00 元，现价 2299.00 元，降幅 8.0%', 'PRICE_DROP', 0, 26, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【iPad Pro 2024】已到货！', 'STOCK_IN', 1, 25, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO notifications (user_id, message, type, is_read, related_product_id, created_at)
VALUES (1, '您关注的商品【MacBook Pro 14】降价了！原价 12999.00 元，现价 11999.00 元，降幅 7.7%', 'PRICE_DROP', 1, 35, DATE_SUB(NOW(), INTERVAL 2 DAY));


-- 插入一条测试通知
INSERT INTO notifications (user_id, message, type, is_read, created_at, updated_at)
VALUES (
           1,
           '📉 您关注的商品【测试商品】降价了！原价 ¥1000.00，现价 ¥800.00，降幅 20.0%',
           'PRICE_DROP',
           0,
           NOW(),
           NOW()
       );

CREATE TABLE IF NOT EXISTS alert_logs (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '告警 ID',
                                          user_id BIGINT NOT NULL COMMENT '用户 ID',
                                          product_id BIGINT NOT NULL COMMENT '商品 ID',
                                          product_name VARCHAR(500) COMMENT '商品名称',
                                          alert_type VARCHAR(50) NOT NULL COMMENT '告警类型：PRICE_DROP=降价，STOCK_CHANGE=库存变化',
                                          original_price DECIMAL(10,2) COMMENT '原始价格',
                                          current_price DECIMAL(10,2) COMMENT '当前价格',
                                          price_drop_percent DECIMAL(5,2) COMMENT '降价百分比',
                                          alert_message TEXT COMMENT '告警消息内容',
                                          is_read TINYINT DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
                                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          INDEX idx_user_id (user_id),
                                          INDEX idx_product_id (product_id),
                                          INDEX idx_is_read (is_read),
                                          INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格告警日志表';

USE price_pulse;

INSERT INTO notifications (user_id, message, type, is_read, created_at, updated_at) VALUES
                                                                                        (1, '📉 您关注的商品【Apple iPhone 15 Pro】降价了！原价 ¥8999.00，现价 ¥7999.00，降幅 11.1%', 'PRICE_DROP', 0, NOW(), NOW()),
                                                                                        (1, '📉 您关注的商品【华为 Mate 60 Pro】降价了！原价 ¥6999.00，现价 ¥6499.00，降幅 7.1%', 'PRICE_DROP', 0, NOW(), NOW()),
                                                                                        (1, '📉 您关注的商品【小米 14 Ultra】降价了！原价 ¥5999.00，现价 ¥5499.00，降幅 8.3%', 'PRICE_DROP', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW());
