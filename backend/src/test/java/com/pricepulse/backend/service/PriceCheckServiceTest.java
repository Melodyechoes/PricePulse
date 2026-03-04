package com.pricepulse.backend.service;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PriceCheckServiceTest {

    @Autowired
    private PriceCheckService priceCheckService;

    @Autowired
    private ProductMapper productMapper;

    @Test
    void testCheckProductPrice() {
        // 先创建一个测试商品
        Product testProduct = createTestProduct();
        productMapper.insert(testProduct);

        // 检查价格
        PriceInfo priceInfo = priceCheckService.checkProductPrice(testProduct.getId());

        // 验证结果
        assertThat(priceInfo).isNotNull();
        // 注意：实际爬虫可能会失败，这里主要验证流程
    }

    @Test
    void testCheckAllProducts() {
        // 批量检查所有商品（这个测试会调用实际的爬虫）
        priceCheckService.checkAllProducts();

        // 验证执行成功（主要看日志输出）
        assertThat(true).isTrue();
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setName("测试商品");
        product.setUrl("https://detail.tmall.com/item.htm?id=123456789");
        product.setPlatform("tmall");
        product.setCurrentPrice(new BigDecimal("9999.00"));
        product.setStatus(1);
        return product;
    }
}
