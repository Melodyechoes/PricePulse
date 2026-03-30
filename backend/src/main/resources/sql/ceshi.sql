-- 创建测试商品：历史测试商品 001
INSERT INTO products (
    name,
    url,
    image_url,
    platform,
    platform_id,
    brand,
    category,
    current_price,
    original_price,
    discount_rate,
    sales_count,
    rating,
    review_count,
    stock_status,
    description,
    status,
    created_at,
    updated_at,
    last_checked
) VALUES (
             '历史测试商品 001',
             'https://item.jd.com/test001.html',
             'https://via.placeholder.com/400x400.png?text=Test+Product',
             'jd',
             'test001',
             '测试品牌',
             'digital',
             2999.00,
             3999.00,
             75.00,
             1000,
             4.8,
             500,
             1,
             '这是一个用于测试价格历史的虚拟商品',
             1,
             NOW() - INTERVAL 30 DAY,
             NOW(),
             NOW()
         );

-- 获取刚插入的商品 ID
SET @product_id = LAST_INSERT_ID();

-- 插入 30 天的价格历史记录（使用 AUTO，因为数据库 ENUM 只支持'AUTO'和'MANUAL'）

-- 第 1 天（30 天前）- 原价
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source)
VALUES (@product_id, 3999.00, 3999.00, 100.00, 'CNY', NOW() - INTERVAL 30 DAY, 'AUTO');

-- 第 2 天（29 天前）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source)
VALUES (@product_id, 3899.00, 3999.00, 97.50, 'CNY', NOW() - INTERVAL 29 DAY, 'AUTO');

-- 第 3 天（28 天前）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source)
VALUES (@product_id, 3799.00, 3999.00, 95.00, 'CNY', NOW() - INTERVAL 28 DAY, 'AUTO');

-- 第 4-10 天（价格小幅波动）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id, 3750.00, 3999.00, 93.77, 'CNY', NOW() - INTERVAL 27 DAY, 'AUTO'),
                                                                                                               (@product_id, 3699.00, 3999.00, 92.50, 'CNY', NOW() - INTERVAL 26 DAY, 'AUTO'),
                                                                                                               (@product_id, 3650.00, 3999.00, 91.27, 'CNY', NOW() - INTERVAL 25 DAY, 'AUTO'),
                                                                                                               (@product_id, 3699.00, 3999.00, 92.50, 'CNY', NOW() - INTERVAL 24 DAY, 'AUTO'),
                                                                                                               (@product_id, 3720.00, 3999.00, 93.02, 'CNY', NOW() - INTERVAL 23 DAY, 'AUTO'),
                                                                                                               (@product_id, 3680.00, 3999.00, 92.02, 'CNY', NOW() - INTERVAL 22 DAY, 'AUTO'),
                                                                                                               (@product_id, 3650.00, 3999.00, 91.27, 'CNY', NOW() - INTERVAL 21 DAY, 'AUTO');

-- 第 11-20 天（价格继续下降）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id, 3599.00, 3999.00, 90.00, 'CNY', NOW() - INTERVAL 20 DAY, 'AUTO'),
                                                                                                               (@product_id, 3550.00, 3999.00, 88.77, 'CNY', NOW() - INTERVAL 19 DAY, 'AUTO'),
                                                                                                               (@product_id, 3499.00, 3999.00, 87.50, 'CNY', NOW() - INTERVAL 18 DAY, 'AUTO'),
                                                                                                               (@product_id, 3450.00, 3999.00, 86.27, 'CNY', NOW() - INTERVAL 17 DAY, 'AUTO'),
                                                                                                               (@product_id, 3399.00, 3999.00, 85.00, 'CNY', NOW() - INTERVAL 16 DAY, 'AUTO'),
                                                                                                               (@product_id, 3350.00, 3999.00, 83.77, 'CNY', NOW() - INTERVAL 15 DAY, 'AUTO'),
                                                                                                               (@product_id, 3299.00, 3999.00, 82.50, 'CNY', NOW() - INTERVAL 14 DAY, 'AUTO'),
                                                                                                               (@product_id, 3250.00, 3999.00, 81.27, 'CNY', NOW() - INTERVAL 13 DAY, 'AUTO'),
                                                                                                               (@product_id, 3199.00, 3999.00, 80.00, 'CNY', NOW() - INTERVAL 12 DAY, 'AUTO'),
                                                                                                               (@product_id, 3150.00, 3999.00, 78.77, 'CNY', NOW() - INTERVAL 11 DAY, 'AUTO');

-- 第 21-30 天（价格大幅优惠）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id, 3099.00, 3999.00, 77.50, 'CNY', NOW() - INTERVAL 10 DAY, 'AUTO'),
                                                                                                               (@product_id, 3050.00, 3999.00, 76.27, 'CNY', NOW() - INTERVAL 9 DAY, 'AUTO'),
                                                                                                               (@product_id, 2999.00, 3999.00, 75.00, 'CNY', NOW() - INTERVAL 8 DAY, 'AUTO'),
                                                                                                               (@product_id, 2950.00, 3999.00, 73.77, 'CNY', NOW() - INTERVAL 7 DAY, 'AUTO'),
                                                                                                               (@product_id, 2899.00, 3999.00, 72.50, 'CNY', NOW() - INTERVAL 6 DAY, 'AUTO'),
                                                                                                               (@product_id, 2850.00, 3999.00, 71.27, 'CNY', NOW() - INTERVAL 5 DAY, 'AUTO'),
                                                                                                               (@product_id, 2799.00, 3999.00, 70.00, 'CNY', NOW() - INTERVAL 4 DAY, 'AUTO'),
                                                                                                               (@product_id, 2750.00, 3999.00, 68.77, 'CNY', NOW() - INTERVAL 3 DAY, 'AUTO'),
                                                                                                               (@product_id, 2699.00, 3999.00, 67.50, 'CNY', NOW() - INTERVAL 2 DAY, 'AUTO'),
                                                                                                               (@product_id, 2999.00, 3999.00, 75.00, 'CNY', NOW() - INTERVAL 1 DAY, 'AUTO');

-- 今天（最新价格）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source)
VALUES (@product_id, 2999.00, 3999.00, 75.00, 'CNY', NOW(), 'AUTO');

-- 查询验证数据
SELECT
    p.id,
    p.name,
    p.platform,
    p.current_price,
    p.original_price,
    p.discount_rate,
    COUNT(ph.id) as history_count,
    MIN(ph.price) as min_price,
    MAX(ph.price) as max_price,
    AVG(ph.price) as avg_price
FROM products p
         LEFT JOIN price_history ph ON p.id = ph.product_id
WHERE p.name = '历史测试商品 001'
GROUP BY p.id;
