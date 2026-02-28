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

-- Show tables
SHOW TABLES;
