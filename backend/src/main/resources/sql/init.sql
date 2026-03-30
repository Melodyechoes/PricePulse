-- 查看你关注的商品和阈值设置
SELECT
    up.user_id,
    up.product_id,
    p.name AS product_name,
    p.current_price,
    up.price_drop_threshold,
    up.target_price
FROM user_products up
         JOIN products p ON up.product_id = p.id
WHERE up.user_id = 15;  -- 替换成你的用户 ID
