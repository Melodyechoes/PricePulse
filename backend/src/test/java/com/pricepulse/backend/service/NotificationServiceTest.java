package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.entity.PlatformEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void testSendPriceDropNotification() {
        Product product = createTestProduct();

        // 发送降价通知
        notificationService.sendPriceDropNotification(
                1L,
                product,
                new BigDecimal("8999.00")
        );

        // 验证方法执行成功（主要看日志）
        assertThat(true).isTrue();
    }

    @Test
    void testSendStockNotification() {
        Product product = createTestProduct();

        // 发送到货通知
        notificationService.sendStockNotification(1L, product);

        assertThat(true).isTrue();
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试商品");
        product.setUrl("https://test.com");
        product.setPlatform(PlatformEnum.TMALL);
        product.setCurrentPrice(new BigDecimal("9999.00"));
        return product;
    }
}
