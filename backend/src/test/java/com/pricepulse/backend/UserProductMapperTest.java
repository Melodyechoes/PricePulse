package com.pricepulse.backend;

import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.mapper.UserProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserProductMapperTest {

    @Autowired
    private UserProductMapper userProductMapper;

    @Test
    void quickTest() {
        System.out.println("🚀 开始用户关注功能快速测试...");

        try {
            // 使用已存在的测试数据
            UserProduct userProduct = new UserProduct();
            userProduct.setUserId(1L);   // 使用已存在的测试用户
            userProduct.setProductId(1L); // 使用已存在的测试商品
            userProduct.setTargetPrice(new BigDecimal("9999.00"));
            userProduct.setNotificationEnabled(1);
            userProduct.setPriceDropThreshold(new BigDecimal("5.00"));

            // 测试插入
            int result = userProductMapper.insert(userProduct);
            assertThat(result).isEqualTo(1);
            assertThat(userProduct.getId()).isNotNull();
            System.out.println("✅ 关注关系创建成功，ID: " + userProduct.getId());

            // 测试查询
            UserProduct found = userProductMapper.selectById(userProduct.getId());
            assertThat(found).isNotNull();
            System.out.println("✅ 关注关系查询成功");

            System.out.println("🎉 用户关注功能测试通过！");

        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
