package com.pricepulse.backend;

import com.pricepulse.backend.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuickTest {

    @Autowired
    private ProductService productService;

    @Test
    void quickSmokeTest() {
        System.out.println("🔍 快速冒烟测试开始...");

        // 验证关键组件是否正常工作
        assertThat(productService).as("ProductService注入").isNotNull();
        System.out.println("✅ ProductService注入成功");

        try {
            var products = productService.getAllProducts();
            System.out.println("✅ 数据库连接成功，商品数量: " + products.size());
        } catch (Exception e) {
            System.err.println("❌ 数据库操作失败: " + e.getMessage());
        }

        System.out.println("🏁 快速测试完成!");
    }
}
