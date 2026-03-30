-- 创建测试商品：历史测试商品 002（手机类）
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
             '历史测试商品 002',
             'https://detail.tmall.com/item.htm?id=628947512345',
             'https://img.alicdn.com/imgextra/i2/628947512345.jpg',
             'tmall',
             '628947512345',
             '华为',
             'digital',
             4299.00,
             5999.00,
             71.67,
             2500,
             4.9,
             1200,
             1,
             '华为旗舰手机，用于测试价格历史的虚拟商品',
             1,
             NOW() - INTERVAL 45 DAY,
             NOW(),
             NOW()
         );

-- 获取刚插入的商品 ID
SET @product_id_002 = LAST_INSERT_ID();

-- 插入 45 天的价格历史记录（更长的时间跨度）

-- 第 1-10 天（原价销售期）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 45 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 44 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 43 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 42 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 41 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 40 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 39 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 38 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 37 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 36 DAY, 'AUTO');

-- 第 11-20 天（首次促销，降价 10%）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id_002, 5699.00, 5999.00, 95.00, 'CNY', NOW() - INTERVAL 35 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5599.00, 5999.00, 93.33, 'CNY', NOW() - INTERVAL 34 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5499.00, 5999.00, 91.67, 'CNY', NOW() - INTERVAL 33 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5399.00, 5999.00, 90.00, 'CNY', NOW() - INTERVAL 32 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5399.00, 5999.00, 90.00, 'CNY', NOW() - INTERVAL 31 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5399.00, 5999.00, 90.00, 'CNY', NOW() - INTERVAL 30 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5399.00, 5999.00, 90.00, 'CNY', NOW() - INTERVAL 29 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5399.00, 5999.00, 90.00, 'CNY', NOW() - INTERVAL 28 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5399.00, 5999.00, 90.00, 'CNY', NOW() - INTERVAL 27 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5399.00, 5999.00, 90.00, 'CNY', NOW() - INTERVAL 26 DAY, 'AUTO');

-- 第 21-30 天（恢复原价）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 25 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 24 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 23 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 22 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 21 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 20 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 19 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 18 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 17 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 5999.00, 5999.00, 100.00, 'CNY', NOW() - INTERVAL 16 DAY, 'AUTO');

-- 第 31-40 天（大型促销，大幅降价 25%）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id_002, 4999.00, 5999.00, 83.33, 'CNY', NOW() - INTERVAL 15 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4899.00, 5999.00, 81.67, 'CNY', NOW() - INTERVAL 14 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4799.00, 5999.00, 80.00, 'CNY', NOW() - INTERVAL 13 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4699.00, 5999.00, 78.33, 'CNY', NOW() - INTERVAL 12 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4599.00, 5999.00, 76.67, 'CNY', NOW() - INTERVAL 11 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4499.00, 5999.00, 75.00, 'CNY', NOW() - INTERVAL 10 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4399.00, 5999.00, 73.33, 'CNY', NOW() - INTERVAL 9 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4299.00, 5999.00, 71.67, 'CNY', NOW() - INTERVAL 8 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4199.00, 5999.00, 70.00, 'CNY', NOW() - INTERVAL 7 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4099.00, 5999.00, 68.33, 'CNY', NOW() - INTERVAL 6 DAY, 'AUTO');

-- 第 41-45 天（促销尾声，小幅回升）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
                                                                                                               (@product_id_002, 4199.00, 5999.00, 70.00, 'CNY', NOW() - INTERVAL 5 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4299.00, 5999.00, 71.67, 'CNY', NOW() - INTERVAL 4 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4399.00, 5999.00, 73.33, 'CNY', NOW() - INTERVAL 3 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4299.00, 5999.00, 71.67, 'CNY', NOW() - INTERVAL 2 DAY, 'AUTO'),
                                                                                                               (@product_id_002, 4299.00, 5999.00, 71.67, 'CNY', NOW() - INTERVAL 1 DAY, 'AUTO');

-- 今天（当前价格）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source)
VALUES (@product_id_002, 4299.00, 5999.00, 71.67, 'CNY', NOW(), 'AUTO');

-- 查询验证数据
SELECT
    p.id,
    p.name,
    p.platform,
    p.brand,
    p.current_price,
    p.original_price,
    p.discount_rate,
    COUNT(ph.id) as history_count,
    MIN(ph.price) as min_price,
    MAX(ph.price) as max_price,
    AVG(ph.price) as avg_price,
    DATEDIFF(NOW(), MIN(ph.checked_at)) as days_span
FROM products p
         LEFT JOIN price_history ph ON p.id = ph.product_id
WHERE p.name = '历史测试商品 002'
GROUP BY p.id;
