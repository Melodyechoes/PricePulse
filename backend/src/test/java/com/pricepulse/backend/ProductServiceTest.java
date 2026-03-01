package com.pricepulse.backend;

import com.pricepulse.backend.common.entity.PlatformEnum;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void testAddProductSuccess() {
        Product product = createValidProduct();

        Product savedProduct = productService.addProduct(product);

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("测试商品");
        assertThat(savedProduct.getStatus()).isEqualTo(1);
    }

    @Test
    void testAddProductWithInvalidData() {
        Product product = new Product();
        // 不设置必要字段，应该抛出异常

        assertThrows(BusinessException.class, () -> {
            productService.addProduct(product);
        });
    }

    @Test
    void testGetProductByIdSuccess() {
        // 先添加一个商品
        Product product = createValidProduct();
        Product savedProduct = productService.addProduct(product);

        // 查询该商品
        Product foundProduct = productService.getProductById(savedProduct.getId());

        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getId()).isEqualTo(savedProduct.getId());
        assertThat(foundProduct.getName()).isEqualTo("测试商品");
    }

    @Test
    void testGetProductByIdNotFound() {
        assertThrows(BusinessException.class, () -> {
            productService.getProductById(999999L); // 不存在的ID
        });
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = productService.getAllProducts();
        assertThat(products).isNotNull();
        // 验证返回的是有效的商品列表
        for (Product product : products) {
            assertThat(product.getId()).isNotNull();
        }
    }

    @Test
    void testUpdateProductSuccess() {
        // 先添加商品
        Product product = createValidProduct();
        Product savedProduct = productService.addProduct(product);

        // 更新商品信息
        savedProduct.setName("更新后的商品名称");
        savedProduct.setCurrentPrice(new BigDecimal("199.99"));

        Product updatedProduct = productService.updateProduct(savedProduct);

        assertThat(updatedProduct.getName()).isEqualTo("更新后的商品名称");
        assertThat(updatedProduct.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("199.99"));
    }

    @Test
    void testDeleteProductSuccess() {
        // 先添加商品
        Product product = createValidProduct();
        Product savedProduct = productService.addProduct(product);

        // 删除商品
        productService.deleteProduct(savedProduct.getId());

        // 验证商品已被软删除（status = 0）
        Product deletedProduct = productService.getProductById(savedProduct.getId());
        assertThat(deletedProduct.getStatus()).isEqualTo(0);
    }

    @Test
    void testGetProductsByCategory() {
        List<Product> products = productService.getProductsByCategory("手机");
        assertThat(products).isNotNull();
        // 验证类别筛选
        for (Product product : products) {
            if (product.getCategory() != null) {
                assertThat(product.getCategory()).isEqualTo("手机");
            }
        }
    }

    @Test
    void testGetProductsByPriceRange() {
        List<Product> products = productService.getProductsByPriceRange(
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

    @Test
    void testInvalidPriceRange() {
        assertThrows(BusinessException.class, () -> {
            productService.getProductsByPriceRange(
                    new BigDecimal("10000"),  // min > max
                    new BigDecimal("5000")
            );
        });
    }

    /**
     * 创建有效的测试商品
     */
    private Product createValidProduct() {
        Product product = new Product();
        product.setName("测试商品");
        product.setUrl("https://test.com/product/123");
        product.setPlatform(PlatformEnum.TAOBAO);
        product.setCurrentPrice(new BigDecimal("99.99"));
        product.setBrand("测试品牌");
        product.setCategory("测试分类");
        return product;
    }
}
