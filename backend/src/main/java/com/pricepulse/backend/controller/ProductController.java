package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 添加商品
     */
    @PostMapping
    public Result<Product> addProduct(@Valid @RequestBody Product product) {
        try {
            Product savedProduct = productService.addProduct(product);
            return Result.success("商品添加成功", savedProduct);
        } catch (Exception e) {
            log.error("添加商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID查询商品
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return Result.success(product);
        } catch (Exception e) {
            log.error("查询商品失败，ID: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有商品
     */
    @GetMapping
    public Result<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id,
                                         @Valid @RequestBody Product product) {
        try {
            product.setId(id);
            Product updatedProduct = productService.updateProduct(product);
            return Result.success("商品更新成功", updatedProduct);
        } catch (Exception e) {
            log.error("更新商品失败，ID: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return Result.success(); // 修复：使用无参的成功方法
        } catch (Exception e) {
            log.error("删除商品失败，ID: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据分类查询商品
     */
    @GetMapping("/category/{category}")
    public Result<List<Product>> getProductsByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            return Result.success(products);
        } catch (Exception e) {
            log.error("按分类查询商品失败，分类: {}", category, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据品牌查询商品
     */
    @GetMapping("/brand/{brand}")
    public Result<List<Product>> getProductsByBrand(@PathVariable String brand) {
        try {
            List<Product> products = productService.getProductsByBrand(brand);
            return Result.success(products);
        } catch (Exception e) {
            log.error("按品牌查询商品失败，品牌: {}", brand, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据价格范围查询商品
     */
    @GetMapping("/price")
    public Result<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        try {
            List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
            return Result.success(products);
        } catch (Exception e) {
            log.error("按价格范围查询商品失败", e);
            return Result.error(e.getMessage());
        }
    }
}
