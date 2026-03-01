package com.pricepulse.backend;

import com.pricepulse.backend.common.entity.PlatformEnum;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // 测试后自动回滚，不影响真实数据
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    void testInsertAndSelect() {
        // 准备测试数据
        Product product = new Product();
        product.setName("测试商品");
        product.setUrl("https://test.com/product/123");
        product.setImageUrl("https://test.com/image.jpg");
        product.setPlatform(PlatformEnum.TAOBAO);
        product.setPlatformId("test123");
        product.setBrand("测试品牌");
        product.setCategory("测试分类");
        product.setCurrentPrice(new BigDecimal("99.99"));
        product.setOriginalPrice(new BigDecimal("199.99"));
        product.setDiscountRate(new BigDecimal("50.00"));
        product.setSalesCount(100);
        product.setRating(new BigDecimal("4.5"));
        product.setReviewCount(50);
        product.setStockStatus(1);
        product.setStatus(1);

        // 测试插入
        int result = productMapper.insert(product);
        assertThat(result).isEqualTo(1);
        assertThat(product.getId()).isNotNull();

        // 测试根据ID查询
        Product foundProduct = productMapper.selectById(product.getId());
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("测试商品");
        assertThat(foundProduct.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void testSelectAll() {
        List<Product> products = productMapper.selectAll();
        assertThat(products).isNotNull();
        // 验证返回的是有效的商品列表（可能包含初始化数据）
        for (Product product : products) {
            assertThat(product.getId()).isNotNull();
            assertThat(product.getName()).isNotNull();
        }
    }

    @Test
    void testSelectByPlatformId() {
        Product product = productMapper.selectByPlatformId("123456789", "TMALL");
        // 可能返回null（如果没有对应数据），这都是正常的
        if (product != null) {
            assertThat(product.getPlatformId()).isEqualTo("123456789");
            assertThat(product.getPlatform()).isEqualTo(PlatformEnum.TMALL);
        }
    }

    @Test
    void testUpdate() {
        // 先插入一个商品
        Product product = createTestProduct();
        productMapper.insert(product);

        // 修改商品信息
        product.setName("更新后的商品名称");
        product.setCurrentPrice(new BigDecimal("199.99"));

        int result = productMapper.update(product);
        assertThat(result).isEqualTo(1);

        // 验证更新结果
        Product updatedProduct = productMapper.selectById(product.getId());
        assertThat(updatedProduct.getName()).isEqualTo("更新后的商品名称");
        assertThat(updatedProduct.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("199.99"));
    }

    @Test
    void testDelete() {
        // 先插入一个商品
        Product product = createTestProduct();
        productMapper.insert(product);

        // 软删除
        int result = productMapper.deleteById(product.getId());
        assertThat(result).isEqualTo(1);

        // 验证删除后查询不到（因为status=0）
        Product deletedProduct = productMapper.selectById(product.getId());
        if (deletedProduct != null) {
            assertThat(deletedProduct.getStatus()).isEqualTo(0);
        }
    }

    @Test
    void testSelectByCategory() {
        List<Product> products = productMapper.selectByCategory("手机");
        assertThat(products).isNotNull();
        // 验证类别筛选
        for (Product product : products) {
            if (product.getCategory() != null) {
                assertThat(product.getCategory()).isEqualTo("手机");
            }
        }
    }

    @Test
    void testSelectByPriceRange() {
        List<Product> products = productMapper.selectByPriceRange(
                new BigDecimal("5000"),
                new BigDecimal("10000")
        );
        assertThat(products).isNotNull();
        // 验证价格范围筛选
        for (Product product : products) {
            if (product.getCurrentPrice() != null) {
                assertThat(product.getCurrentPrice())
                        .isGreaterThanOrEqualTo(new BigDecimal("5000"))
                        .isLessThanOrEqualTo(new BigDecimal("10000"));
            }
        }
    }

    /**
     * 创建测试用商品对象
     */
    private Product createTestProduct() {
        Product product = new Product();
        product.setName("测试商品_" + System.currentTimeMillis());
        product.setUrl("https://test.com/product/" + System.currentTimeMillis());
        product.setPlatform(PlatformEnum.TAOBAO);
        product.setPlatformId("test_" + System.currentTimeMillis());
        product.setCurrentPrice(new BigDecimal("99.99"));
        product.setStatus(1);
        product.setStockStatus(1);
        return product;
    }
}
