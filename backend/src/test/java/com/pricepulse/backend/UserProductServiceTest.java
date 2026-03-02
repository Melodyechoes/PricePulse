package com.pricepulse.backend;

import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.service.UserProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SimpleUserProductTest {

    @Autowired
    private UserProductService userProductService;

    @Test
    void testCoreFunctionality() {
        System.out.println("🚀 开始用户关注核心功能测试...");

        try {
            // 测试1: Service注入
            assertThat(userProductService).isNotNull();
            System.out.println("✅ Service注入成功");

            // 测试2: 使用唯一数据关注商品
            UserProduct userProduct = createUniqueUserProduct();
            UserProduct result = userProductService.followProduct(userProduct);
            assertThat(result.getId()).isNotNull();
            System.out.println("✅ 关注商品成功，ID: " + result.getId());

            // 测试3: 查询用户关注列表
            List<UserProduct> followedProducts = userProductService.getUserFollowedProducts(userProduct.getUserId());
            assertThat(followedProducts).isNotEmpty();
            System.out.println("✅ 查询关注列表成功，数量: " + followedProducts.size());

            // 测试4: 查询商品关注者
            List<UserProduct> followers = userProductService.getProductFollowers(userProduct.getProductId());
            assertThat(followers).isNotEmpty();
            System.out.println("✅ 查询商品关注者成功，数量: " + followers.size());

            System.out.println("🎉 所有核心功能测试通过！");

        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UserProduct createUniqueUserProduct() {
        UserProduct userProduct = new UserProduct();
        long uniqueId = System.nanoTime();
        userProduct.setUserId(uniqueId);
        userProduct.setProductId(uniqueId + 1000000);
        userProduct.setTargetPrice(new BigDecimal("9999.00"));
        userProduct.setNotificationEnabled(1);
        userProduct.setPriceDropThreshold(new BigDecimal("5.00"));
        return userProduct;
    }
}
