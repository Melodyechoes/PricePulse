-- 清除旧的测试数据（可选）
DELETE FROM price_history WHERE product_id = 1 AND checked_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 添加最近 7 天的价格数据（每天一条）
INSERT INTO price_history (product_id, checked_at, price, original_price, discount_rate, currency, source)
VALUES
    (1, DATE_SUB(NOW(), INTERVAL 7 DAY), 10999.00, 11999.00, 8.33, 'CNY', 'manual'),
    (1, DATE_SUB(NOW(), INTERVAL 6 DAY), 10799.00, 11999.00, 10.00, 'CNY', 'manual'),
    (1, DATE_SUB(NOW(), INTERVAL 5 DAY), 10599.00, 11999.00, 11.67, 'CNY', 'manual'),
    (1, DATE_SUB(NOW(), INTERVAL 4 DAY), 10399.00, 11999.00, 13.33, 'CNY', 'manual'),
    (1, DATE_SUB(NOW(), INTERVAL 3 DAY), 10199.00, 11999.00, 15.00, 'CNY', 'manual'),
    (1, DATE_SUB(NOW(), INTERVAL 2 DAY), 9999.00, 11999.00, 16.67, 'CNY', 'manual'),
    (1, DATE_SUB(NOW(), INTERVAL 1 DAY), 9799.00, 11999.00, 18.33, 'CNY', 'manual');

-- 验证插入的数据
SELECT
    ph.id,
    ph.product_id,
    p.name AS product_name,
    ph.checked_at,
    ph.price,
    ph.original_price
FROM price_history ph
         LEFT JOIN products p ON ph.product_id = p.id
WHERE ph.product_id = 1
ORDER BY ph.checked_at DESC;
