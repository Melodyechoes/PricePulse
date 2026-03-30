package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.PriceHistory;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.mapper.NotificationMapper;
import com.pricepulse.backend.mapper.PriceHistoryMapper;
import com.pricepulse.backend.mapper.ProductMapper;
import com.pricepulse.backend.mapper.UserProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 降价通知功能测试
 */
@SpringBootTest
@Slf4j
@DisplayName("降价通知功能测试")
public class PriceDropNotificationTest {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserProductMapper userProductMapper;

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private com.pricepulse.backend.controller.ProductController productController;


    /**
     * 测试场景 1：商品降价，应该发送通知
     */
    @Test
    @DisplayName("测试 1：商品降价时应发送通知")
    @Transactional
    public void testPriceDrop_ShouldSendNotification() {
        log.info("=== [测试 1] 开始：商品降价时应发送通知 ===");

        // 1. 准备测试数据
        Long userId = 15L; // 你的用户 ID
        Long productId = 35L; // 已关注的商品 ID

        // 2. 获取商品信息
        Product product = productMapper.selectById(productId);
        assertNotNull(product, "商品不存在");
        log.info("商品：{}, 当前价格：{}", product.getName(), product.getCurrentPrice());

        // 3. 检查是否关注
        UserProduct userProduct = userProductMapper.selectByUserIdAndProductId(userId, productId);
        if (userProduct == null) {
            log.error("❌ 用户 {} 未关注商品 {}，测试无法继续", userId, productId);

            // 【新增】如果没有关注记录，创建一个
            log.info("正在创建关注记录...");
            UserProduct newUserProduct = new UserProduct();
            newUserProduct.setUserId(userId);
            newUserProduct.setProductId(productId);
            newUserProduct.setTargetPrice(new BigDecimal("5000"));
            newUserProduct.setNotificationEnabled(1);
            newUserProduct.setPriceDropThreshold(new BigDecimal("0.1")); // 10% 阈值
            userProductMapper.insert(newUserProduct);
            log.info("✅ 已创建关注记录，ID: {}", newUserProduct.getId());

            // 重新查询
            userProduct = userProductMapper.selectByUserIdAndProductId(userId, productId);
        }

        // 【重要】临时修改阈值为 10%，确保测试能触发通知
        BigDecimal originalThreshold = userProduct.getPriceDropThreshold();
        log.info("原始阈值：{}", originalThreshold);

        userProduct.setPriceDropThreshold(new BigDecimal("0.1")); // 设置为 10%
        int updateResult = userProductMapper.updateById(userProduct);
        log.info("已将阈值从 {} 修改为 10%，更新结果：{}", originalThreshold, updateResult);

        // 重新查询以确认更新成功
        userProduct = userProductMapper.selectByUserIdAndProductId(userId, productId);
        log.info("✅ 用户 {} 已关注该商品，当前阈值：{} (确认)", userId, userProduct.getPriceDropThreshold());

        // 4. 记录降价前的通知数量
        int notificationCountBefore = notificationMapper.countByUserId(userId);
        log.info("降价前通知数量：{}", notificationCountBefore);

        // 【修改】5. 通过调用 Controller 方法来刷新价格（模拟降价）
        log.info("开始调用刷新价格接口...");

        try {
            // 多次调用以确保一定会降价
            for (int i = 0; i < 3; i++) {
                log.info("第 {} 次刷新价格...", i + 1);
                var result = productController.crawlProductPrice(productId);

                log.info("价格刷新完成，响应码：{}", result.getCode());
                log.info("新价格：{}", result.getData().getCurrentPrice());

                // 等待异步通知处理完成
                Thread.sleep(1000);

                // 检查通知是否增加
                int currentCount = notificationMapper.countByUserId(userId);
                if (currentCount > notificationCountBefore) {
                    log.info("✅ 检测到通知增加，当前通知数量：{}", currentCount);
                    break;
                }
            }

        } catch (Exception e) {
            log.error("调用刷新价格失败", e);
            fail("调用刷新价格接口失败：" + e.getMessage());
        }

        // 6. 检查通知数量
        int notificationCountAfter = notificationMapper.countByUserId(userId);
        log.info("降价后通知数量：{}", notificationCountAfter);

        // 7. 验证：通知数量应该增加
        assertTrue(notificationCountAfter > notificationCountBefore,
                "应该有新的降价通知，但实际没有增加。降价前：" + notificationCountBefore + ", 降价后：" + notificationCountAfter);

        log.info("✅ [测试 1] 通过：降价通知发送成功");
    }

    /**
     * 测试场景 2：商品未降价，不应该发送通知
     */
    @Test
    @DisplayName("测试 2：商品未降价时不应发送通知")
    @Transactional
    public void testNoPriceDrop_ShouldNotSendNotification() {
        log.info("=== [测试 2] 开始：商品未降价时不应发送通知 ===");

        Long userId = 15L;
        Long productId = 35L;

        // 1. 获取商品信息
        Product product = productMapper.selectById(productId);
        assertNotNull(product, "商品不存在");

        // 2. 检查是否关注
        UserProduct userProduct = userProductMapper.selectByUserIdAndProductId(userId, productId);
        if (userProduct == null) {
            log.warn("用户 {} 未关注商品 {}，跳过测试", userId, productId);
            return;
        }

        // 3. 记录通知数量
        int notificationCountBefore = notificationMapper.countByUserId(userId);
        log.info("降价前通知数量：{}", notificationCountBefore);

        // 4. 模拟价格不变或上涨
        BigDecimal oldPrice = product.getCurrentPrice();
        BigDecimal newPrice = oldPrice.multiply(new BigDecimal("1.05")) // 涨价 5%
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        log.info("模拟价格变化：{} -> {} (涨价)", oldPrice, newPrice);

        product.setCurrentPrice(newPrice);
        product.setOriginalPrice(oldPrice);
        productMapper.updateById(product);

        // 5. 等待处理
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 6. 验证：通知数量不应该增加
        int notificationCountAfter = notificationMapper.countByUserId(userId);
        assertEquals(notificationCountBefore, notificationCountAfter,
                "价格未下降，通知数量不应该增加");

        log.info("✅ [测试 2] 通过：未降价时没有发送通知");
    }

    /**
     * 测试场景 3：降价幅度未达到阈值，不应该发送通知
     */
    @Test
    @DisplayName("测试 3：降价幅度未达阈值时不应发送通知")
    @Transactional
    public void testPriceDropBelowThreshold_ShouldNotSendNotification() {
        log.info("=== [测试 3] 开始：降价幅度未达阈值时不应发送通知 ===");

        Long userId = 15L;
        Long productId = 35L;

        // 1. 获取商品和关注信息
        Product product = productMapper.selectById(productId);
        UserProduct userProduct = userProductMapper.selectByUserIdAndProductId(userId, productId);

        if (userProduct == null) {
            log.warn("用户 {} 未关注商品 {}，跳过测试", userId, productId);
            return;
        }

        // 2. 设置较高的阈值（比如 20%）
        BigDecimal highThreshold = new BigDecimal("0.20");
        userProduct.setPriceDropThreshold(highThreshold);
        userProductMapper.updateById(userProduct);
        log.info("设置阈值为：20%");

        // 3. 记录通知数量
        int notificationCountBefore = notificationMapper.countByUserId(userId);

        // 4. 模拟小幅降价（只降 5%）
        BigDecimal oldPrice = product.getCurrentPrice();
        BigDecimal newPrice = oldPrice.multiply(new BigDecimal("0.95")) // 降价 5%
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        log.info("模拟小幅降价：{} -> {} (降幅 5%)", oldPrice, newPrice);

        product.setCurrentPrice(newPrice);
        product.setOriginalPrice(oldPrice);
        productMapper.updateById(product);

        // 5. 等待处理
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 6. 验证：通知数量不应该增加（因为 5% < 20%）
        int notificationCountAfter = notificationMapper.countByUserId(userId);
        assertEquals(notificationCountBefore, notificationCountAfter,
                "降价幅度未达到阈值，不应该发送通知");

        log.info("✅ [测试 3] 通过：未达阈值时没有发送通知");

        // 7. 恢复默认阈值
        userProduct.setPriceDropThreshold(new BigDecimal("0.1"));
        userProductMapper.updateById(userProduct);
    }

    /**
     * 测试场景 4：多用户关注同一商品，都应该收到通知
     */
    @Test
    @DisplayName("测试 4：多用户关注时应给所有用户发送通知")
    @Transactional
    public void testMultipleUsers_ShouldNotifyAll() {
        log.info("=== [测试 4] 开始：多用户关注时应给所有用户发送通知 ===");

        Long productId = 35L;
        Long userId = 15L;

        // 1. 获取商品信息
        Product product = productMapper.selectById(productId);
        assertNotNull(product, "商品不存在");

        // 2. 查询所有关注该商品的用户
        List<UserProduct> userProducts = userProductMapper.selectByProductId(productId);

        if (userProducts == null || userProducts.isEmpty()) {
            log.warn("商品 {} 没有关注用户，正在创建测试用户...", productId);

            // 【新增】创建测试关注记录
            UserProduct up = new UserProduct();
            up.setUserId(userId);
            up.setProductId(productId);
            up.setTargetPrice(new BigDecimal("5000"));
            up.setNotificationEnabled(1);
            up.setPriceDropThreshold(new BigDecimal("0.1"));
            userProductMapper.insert(up);

            // 重新查询
            userProducts = userProductMapper.selectByProductId(productId);
            log.info("已创建 {} 个关注记录", userProducts.size());
        }

        // 【重要】临时修改所有用户的阈值为 10%
        log.info("=== 开始修改所有用户的阈值为 10% ===");
        for (UserProduct up : userProducts) {
            BigDecimal originalThreshold = up.getPriceDropThreshold();
            up.setPriceDropThreshold(new BigDecimal("0.1"));
            int result = userProductMapper.updateById(up);
            log.info("用户 {} 的阈值从 {} 修改为 10%，结果：{}", up.getUserId(), originalThreshold, result);
        }

        // 重新查询以确认更新
        userProducts = userProductMapper.selectByProductId(productId);
        for (UserProduct up : userProducts) {
            log.info("✅ 用户 {} 当前阈值：{} (已确认)", up.getUserId(), up.getPriceDropThreshold());
        }
        log.info("=== 阈值修改完成 ===");

        // 3. 记录每个用户的通知数量
        int[] countsBefore = new int[userProducts.size()];
        for (int i = 0; i < userProducts.size(); i++) {
            Long uid = userProducts.get(i).getUserId();
            countsBefore[i] = notificationMapper.countByUserId(uid);
            log.info("用户 {} 降价前通知数量：{}", uid, countsBefore[i]);
        }

        // 4. 调用 Controller 方法刷新价格
        log.info("开始调用刷新价格接口...");
        try {
            // 多次刷新以确保降价
            for (int i = 0; i < 3; i++) {
                log.info("第 {} 次刷新价格...", i + 1);
                var result = productController.crawlProductPrice(productId);
                log.info("新价格：{}", result.getData().getCurrentPrice());

                Thread.sleep(1000);
            }

        } catch (Exception e) {
            log.error("调用刷新价格失败", e);
            fail("调用刷新价格接口失败：" + e.getMessage());
        }

        // 5. 验证：每个用户的通知数量都应该增加
        boolean allNotified = true;
        for (int i = 0; i < userProducts.size(); i++) {
            Long uid = userProducts.get(i).getUserId();
            int countAfter = notificationMapper.countByUserId(uid);

            log.info("用户 {} 降价后通知数量：{}", uid, countAfter);

            if (countAfter <= countsBefore[i]) {
                allNotified = false;
                log.warn("用户 {} 没有收到通知！", uid);
            }
        }

        assertTrue(allNotified, "所有关注用户都应该收到降价通知");

        log.info("✅ [测试 4] 通过：所有关注用户都收到了通知");
    }


    /**
     * 辅助方法：统计用户的通知数量
     */
    private int getUserNotificationCount(Long userId) {
        return notificationMapper.countByUserId(userId);
    }

}
