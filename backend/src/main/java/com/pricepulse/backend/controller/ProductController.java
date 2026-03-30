package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.common.entity.PriceHistory;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.service.PriceHistoryService;
import com.pricepulse.backend.service.ProductService;
import com.pricepulse.backend.service.crawler.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pricepulse.backend.service.crawler.CrawlerStrategyFactory;



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

    @Autowired
    private CrawlerStrategyFactory crawlerFactory;



    /**
     * 根据 URL 解析商品信息
     */
    @PostMapping("/parse-url")
    public Result<Product> parseProductUrl(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");
            if (url == null || url.trim().isEmpty()) {
                return Result.error("URL 不能为空");
            }

            log.info("解析商品 URL: {}", url);

            // 使用爬虫工厂获取对应平台的爬虫服务
            CrawlerService crawler = crawlerFactory.getCrawler(url);
            String platform = crawlerFactory.getPlatform(url);

            // 爬取价格信息
            PriceInfo priceInfo = crawler.crawlPrice(url);

            // 构建商品对象
            Product product = new Product();
            product.setName(priceInfo.getTitle());
            product.setDescription(priceInfo.getTitle());
            product.setCategory("其他");
            product.setPlatform(platform);
            product.setOriginalPrice(priceInfo.getOriginalPrice() != null ?
                    priceInfo.getOriginalPrice() : priceInfo.getCurrentPrice());
            product.setCurrentPrice(priceInfo.getCurrentPrice());
            product.setDiscountRate(priceInfo.getDiscountRate() != null ?
                    priceInfo.getDiscountRate() : BigDecimal.ZERO);
            product.setUrl(url);
            product.setImageUrl(priceInfo.getImageUrl());

            log.info("解析成功：{}", product.getName());
            return Result.success(product);

        } catch (UnsupportedOperationException e) {
            log.error("不支持的平台", e);
            return Result.error(e.getMessage());
        } catch (BusinessException e) {
            log.error("解析 URL 失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("解析 URL 异常", e);
            return Result.error("服务器内部错误");
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("")
    public Result<Product> addProduct(@RequestBody Product product) {
        try {
            log.info("添加商品：{}", product.getName());

            // 参数校验
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            if (product.getUrl() == null || product.getUrl().trim().isEmpty()) {
                return Result.error("商品链接不能为空");
            }
            if (product.getCurrentPrice() == null) {
                return Result.error("商品价格不能为空");
            }

            // 保存商品
            Product savedProduct = productService.addProduct(product);
            log.info("商品添加成功，ID: {}", savedProduct.getId());

            return Result.success(savedProduct);

        } catch (BusinessException e) {
            log.error("添加商品失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加商品异常", e);
            return Result.error("服务器内部错误");
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
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long userId) {
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

            // 【修改】使用传入的 userId，如果没有则默认为 1
            Long currentUserId = userId != null ? userId : 1L;
            log.info("当前登录用户 ID: {}", currentUserId);

            for (Product product : products) {
                boolean isFollowed = productService.isProductFollowedByUser(product.getId(), currentUserId);
                product.setIsFollowed(isFollowed);
                log.debug("商品 {} - {}: isFollowed={}", product.getId(), product.getName(), isFollowed);
            }

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

    /**
     * 手动触发价格爬取
     */
    @PostMapping("/crawl-price/{productId}")
    public Result<Product> crawlProductPrice(@PathVariable Long productId) {
        try {
            log.info("手动触发价格爬取，productId: {}", productId);

            Product product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }

            // 【演示模式】生成模拟价格变化
            BigDecimal basePrice = product.getOriginalPrice() != null ?
                    product.getOriginalPrice() :
                    (product.getCurrentPrice() != null ? product.getCurrentPrice() : new BigDecimal("1000"));

            // 随机降价 5%-15%
            double randomFactor = 0.85 + Math.random() * 0.15;
            BigDecimal newPrice = basePrice.multiply(new BigDecimal(String.valueOf(randomFactor)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal discountRate = newPrice.divide(basePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            log.info("【演示模式】价格更新：原价 {} -> 新价 {}, 折扣率 {}%",
                    basePrice, newPrice, discountRate);

            // 直接更新价格相关字段
            product.setCurrentPrice(newPrice);
            product.setOriginalPrice(basePrice);
            product.setDiscountRate(discountRate);

            // 记录价格历史
            PriceHistory history = new PriceHistory();
            history.setProductId(productId);
            history.setCheckedAt(java.time.LocalDateTime.now());
            history.setPrice(newPrice);
            history.setOriginalPrice(basePrice);
            history.setDiscountRate(discountRate);
            history.setCurrency("CNY");
            history.setSource("manual");
            priceHistoryService.addPriceHistory(history);

            // 保存更新 - 使用完整更新
            productService.updateProduct(product);

            log.info("价格更新成功");
            return Result.success(product);

        } catch (Exception e) {
            log.error("价格更新失败，productId: {}", productId, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
