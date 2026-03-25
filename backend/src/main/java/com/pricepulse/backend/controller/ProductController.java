package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.entity.PriceHistory;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.service.PriceHistoryService;
import com.pricepulse.backend.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private PriceHistoryService priceHistoryService;
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
     * 获取所有商品（支持搜索、分类、排序）
     */
    @GetMapping
    public Result<List<Product>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        try {
            // 构建查询参数
            Map<String, Object> params = new java.util.HashMap<>();
            if (keyword != null && !keyword.isEmpty()) {
                params.put("keyword", keyword);
            }
            if (category != null && !category.isEmpty()) {
                params.put("category", category);
            }
            if (platform != null && !platform.isEmpty()) {
                params.put("platform", platform);
            }
            if (minPrice != null) {
                params.put("minPrice", minPrice);
            }
            if (maxPrice != null) {
                params.put("maxPrice", maxPrice);
            }

            List<Product> products = productService.searchWithFilters(params);

            // 价格排序
            if ("asc".equals(sort)) {
                products.sort((p1, p2) -> {
                    BigDecimal price1 = p1.getCurrentPrice();
                    BigDecimal price2 = p2.getCurrentPrice();
                    if (price1 == null && price2 == null) return 0;
                    if (price1 == null) return 1;
                    if (price2 == null) return -1;
                    return price1.compareTo(price2);
                });
            } else if ("desc".equals(sort)) {
                products.sort((p1, p2) -> {
                    BigDecimal price1 = p1.getCurrentPrice();
                    BigDecimal price2 = p2.getCurrentPrice();
                    if (price1 == null && price2 == null) return 0;
                    if (price1 == null) return 1;
                    if (price2 == null) return -1;
                    return price2.compareTo(price1);
                });
            }

            log.info("获取商品列表成功，共 {} 个商品", products.size());
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有可筛选的分类和平台
     */
    @GetMapping("/filters")
    public Result<Map<String, Object>> getAvailableFilters() {
        try {
            Map<String, Object> filters = productService.getAvailableFilters();
            return Result.success(filters);
        } catch (Exception e) {
            log.error("获取筛选条件失败", e);
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

    /**
     * 获取商品的价格历史记录
     */
    @GetMapping("/{productId}/price-history")
    public Result<List<PriceHistory>> getPriceHistory(@PathVariable Long productId) {
        try {
            log.info("获取商品价格历史，productId: {}", productId);
            List<PriceHistory> history = priceHistoryService.getPriceHistory(productId);
            log.info("价格历史数据数量：{}", history.size());
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取价格历史失败，productId: {}", productId, e);
            return Result.error(e.getMessage());
        }
    }
}
