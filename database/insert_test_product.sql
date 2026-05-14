-- 插入一个测试商品用于验证价格趋势功能
INSERT INTO products (name, url, image_url, platform, platform_id, brand, category, current_price, original_price, discount_rate, sales_count, rating, review_count, stock_status, description) 
VALUES ('Test Price Trend Product - iPhone 16 Pro', 'https://detail.tmall.com/item.htm?id=test123456', 'https://via.placeholder.com/400x400.png?text=iPhone+16+Pro', 'TMALL', 'test123456', 'Apple', 'digital', 8999.00, 9999.00, 89.99, 5000, 4.95, 2000, 1, 'Virtual product for testing price trend feature');

-- 获取刚插入的商品ID（假设自增ID为54，如果实际不同请调整）
SET @test_product_id = LAST_INSERT_ID();

-- 为该商品插入30天的历史价格数据（模拟真实的价格波动）
INSERT INTO price_history (product_id, price, original_price, discount_rate, currency, checked_at, source) VALUES
(@test_product_id, 9999.00, 9999.00, 100.00, 'CNY', DATE_SUB(NOW(), INTERVAL 30 DAY), 'AUTO'),
(@test_product_id, 9899.00, 9999.00, 99.00, 'CNY', DATE_SUB(NOW(), INTERVAL 29 DAY), 'AUTO'),
(@test_product_id, 9799.00, 9999.00, 98.00, 'CNY', DATE_SUB(NOW(), INTERVAL 28 DAY), 'AUTO'),
(@test_product_id, 9699.00, 9999.00, 97.00, 'CNY', DATE_SUB(NOW(), INTERVAL 27 DAY), 'AUTO'),
(@test_product_id, 9599.00, 9999.00, 96.00, 'CNY', DATE_SUB(NOW(), INTERVAL 26 DAY), 'AUTO'),
(@test_product_id, 9499.00, 9999.00, 95.00, 'CNY', DATE_SUB(NOW(), INTERVAL 25 DAY), 'AUTO'),
(@test_product_id, 9399.00, 9999.00, 94.00, 'CNY', DATE_SUB(NOW(), INTERVAL 24 DAY), 'AUTO'),
(@test_product_id, 9299.00, 9999.00, 93.00, 'CNY', DATE_SUB(NOW(), INTERVAL 23 DAY), 'AUTO'),
(@test_product_id, 9199.00, 9999.00, 92.00, 'CNY', DATE_SUB(NOW(), INTERVAL 22 DAY), 'AUTO'),
(@test_product_id, 9099.00, 9999.00, 91.00, 'CNY', DATE_SUB(NOW(), INTERVAL 21 DAY), 'AUTO'),
(@test_product_id, 8999.00, 9999.00, 90.00, 'CNY', DATE_SUB(NOW(), INTERVAL 20 DAY), 'AUTO'),
(@test_product_id, 8899.00, 9999.00, 89.00, 'CNY', DATE_SUB(NOW(), INTERVAL 19 DAY), 'AUTO'),
(@test_product_id, 8799.00, 9999.00, 88.00, 'CNY', DATE_SUB(NOW(), INTERVAL 18 DAY), 'AUTO'),
(@test_product_id, 8699.00, 9999.00, 87.00, 'CNY', DATE_SUB(NOW(), INTERVAL 17 DAY), 'AUTO'),
(@test_product_id, 8599.00, 9999.00, 86.00, 'CNY', DATE_SUB(NOW(), INTERVAL 16 DAY), 'AUTO'),
(@test_product_id, 8499.00, 9999.00, 85.00, 'CNY', DATE_SUB(NOW(), INTERVAL 15 DAY), 'AUTO'),
(@test_product_id, 8399.00, 9999.00, 84.00, 'CNY', DATE_SUB(NOW(), INTERVAL 14 DAY), 'AUTO'),
(@test_product_id, 8299.00, 9999.00, 83.00, 'CNY', DATE_SUB(NOW(), INTERVAL 13 DAY), 'AUTO'),
(@test_product_id, 8199.00, 9999.00, 82.00, 'CNY', DATE_SUB(NOW(), INTERVAL 12 DAY), 'AUTO'),
(@test_product_id, 8099.00, 9999.00, 81.00, 'CNY', DATE_SUB(NOW(), INTERVAL 11 DAY), 'AUTO'),
(@test_product_id, 7999.00, 9999.00, 80.00, 'CNY', DATE_SUB(NOW(), INTERVAL 10 DAY), 'AUTO'),
(@test_product_id, 8099.00, 9999.00, 81.00, 'CNY', DATE_SUB(NOW(), INTERVAL 9 DAY), 'AUTO'),
(@test_product_id, 8199.00, 9999.00, 82.00, 'CNY', DATE_SUB(NOW(), INTERVAL 8 DAY), 'AUTO'),
(@test_product_id, 8299.00, 9999.00, 83.00, 'CNY', DATE_SUB(NOW(), INTERVAL 7 DAY), 'AUTO'),
(@test_product_id, 8399.00, 9999.00, 84.00, 'CNY', DATE_SUB(NOW(), INTERVAL 6 DAY), 'AUTO'),
(@test_product_id, 8499.00, 9999.00, 85.00, 'CNY', DATE_SUB(NOW(), INTERVAL 5 DAY), 'AUTO'),
(@test_product_id, 8599.00, 9999.00, 86.00, 'CNY', DATE_SUB(NOW(), INTERVAL 4 DAY), 'AUTO'),
(@test_product_id, 8699.00, 9999.00, 87.00, 'CNY', DATE_SUB(NOW(), INTERVAL 3 DAY), 'AUTO'),
(@test_product_id, 8799.00, 9999.00, 88.00, 'CNY', DATE_SUB(NOW(), INTERVAL 2 DAY), 'AUTO'),
(@test_product_id, 8899.00, 9999.00, 89.00, 'CNY', DATE_SUB(NOW(), INTERVAL 1 DAY), 'AUTO'),
(@test_product_id, 8999.00, 9999.00, 90.00, 'CNY', NOW(), 'AUTO');

-- 将测试商品关联到用户ID为1的用户（admin用户）
INSERT INTO user_products (user_id, product_id, target_price, notification_enabled, price_drop_threshold) 
VALUES (1, @test_product_id, NULL, 1, 5.00);

-- 查询验证插入的数据
SELECT '=== 测试商品信息 ===' as info;
SELECT id, name, current_price, original_price, platform, category FROM products WHERE id = @test_product_id;

SELECT '=== 价格历史记录数量 ===' as info;
SELECT COUNT(*) as history_count FROM price_history WHERE product_id = @test_product_id;

SELECT '=== 最近5条价格记录 ===' as info;
SELECT id, price, original_price, discount_rate, checked_at, source FROM price_history WHERE product_id = @test_product_id ORDER BY checked_at DESC LIMIT 5;

SELECT '=== 用户关注关系 ===' as info;
SELECT up.id, u.username, p.name as product_name, up.notification_enabled FROM user_products up JOIN users u ON up.user_id = u.id JOIN products p ON up.product_id = p.id WHERE p.id = @test_product_id;
