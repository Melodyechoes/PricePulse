package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.common.entity.PriceHistory;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.service.PriceHistoryService;
import com.pricepulse.backend.service.ProductService;
import com.pricepulse.backend.service.crawler.CrawlerService;
import com.pricepulse.backend.service.NotificationService;
import com.pricepulse.backend.mapper.UserProductMapper;
import com.pricepulse.backend.common.entity.UserProduct;
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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserProductMapper userProductMapper;

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
     * 根据 ID 查询商品
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
            log.error("按分类查询商品失败，分类：{}", category, e);
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
            log.error("按品牌查询商品失败，品牌：{}", brand, e);
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

            // 【修改】获取基准价格：使用当前价格作为基准
            BigDecimal currentPrice = product.getCurrentPrice();
            if (currentPrice == null) {
                currentPrice = product.getOriginalPrice();
            }
            if (currentPrice == null) {
                currentPrice = new BigDecimal("1000");
            }

            log.info("基准价格：当前价={}, 原价={}",
                    product.getCurrentPrice(), product.getOriginalPrice());

            // 【修改】随机降价 5%-20%（确保每次都降价）
            double randomFactor = 0.80 + Math.random() * 0.20;

            // 确保随机因子小于 1（保证降价）
            if (randomFactor >= 1.0) {
                randomFactor = 0.95;
            }

            BigDecimal newPrice = currentPrice.multiply(new BigDecimal(String.valueOf(randomFactor)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            // 【新增】二次检查：确保新价格一定低于当前价格
            if (newPrice.compareTo(currentPrice) >= 0) {
                // 如果新价格大于或等于当前价格，强制降价 10%
                newPrice = currentPrice.multiply(new BigDecimal("0.9"))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                log.info("检测到价格未下降，强制降价 10%");
            }

            BigDecimal discountRate = newPrice.divide(currentPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            log.info("【演示模式】价格更新：原价 {} -> 新价 {}, 折扣率 {}%",
                    currentPrice, newPrice, discountRate);

            // 【修改】检查是否降价并发送通知（与当前价格比较）
            BigDecimal oldPrice = product.getCurrentPrice();

            log.info("=== [降价通知] 开始检查 ===");
            log.info("商品 ID: {}, 商品名称：{}", product.getId(), product.getName());
            log.info("当前价格：{}, 新价格：{}", oldPrice, newPrice);
            log.info("是否降价：{}", (oldPrice != null && newPrice.compareTo(oldPrice) < 0));

            // 直接更新价格相关字段
            product.setCurrentPrice(newPrice);
            product.setOriginalPrice(currentPrice); // 保持原价为之前的价格
            product.setDiscountRate(discountRate);

            // 【新增】如果降价了，触发通知
            if (oldPrice != null && newPrice.compareTo(oldPrice) < 0) {
                log.info("检测到降价：商品 {} 从 {} 降到 {}", product.getName(), oldPrice, newPrice);

                // 触发降价通知
                triggerPriceDropNotification(product, oldPrice, newPrice);
            } else {
                log.warn("价格未下降或 oldPrice 为 null，跳过通知");
            }

            // 记录价格历史
            PriceHistory history = new PriceHistory();
            history.setProductId(productId);
            history.setCheckedAt(java.time.LocalDateTime.now());
            history.setPrice(newPrice);
            history.setOriginalPrice(currentPrice);
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

    /**
     * 触发降价通知
     */
    private void triggerPriceDropNotification(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        try {
            log.info("=== [降价通知] 准备发送通知 ===");
            log.info("商品 ID: {}, 商品名称：{}", product.getId(), product.getName());
            log.info("商品原价：{}, 商品现价：{}", oldPrice, newPrice);

            // 获取关注该商品的所有用户
            var userProducts = userProductMapper.selectByProductId(product.getId());

            log.info("查询到的关注用户数量：{}", userProducts == null ? 0 : userProducts.size());
            if (userProducts != null) {
                for (var up : userProducts) {
                    log.info("  - 用户 ID: {}, 阈值：{}", up.getUserId(), up.getPriceDropThreshold());
                }
            }

            if (userProducts == null || userProducts.isEmpty()) {
                log.debug("商品 {} 暂无关注用户，跳过通知", product.getName());
                return;
            }

            log.info("商品 {} 有 {} 个用户关注，准备发送通知", product.getName(), userProducts.size());

            for (var userProduct : userProducts) {
                // 检查是否达到提醒阈值
                BigDecimal alertThreshold = userProduct.getPriceDropThreshold();
                if (alertThreshold == null) {
                    alertThreshold = new BigDecimal("0.1"); // 默认 10%
                }

                // 计算降价幅度
                BigDecimal dropAmount = oldPrice.subtract(newPrice);
                BigDecimal dropPercent = dropAmount.divide(oldPrice, 4, java.math.BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));

                log.info("用户 {} 关注商品 {}，降价幅度 {}%，阈值 {}%",
                        userProduct.getUserId(), product.getName(), dropPercent, alertThreshold.multiply(new BigDecimal("100")));

                // 【修复】如果降价幅度达到阈值，发送通知
                // alertThreshold 是小数形式（如 0.1 表示 10%），dropPercent 是百分比形式（如 12.1 表示 12.1%）
                if (dropPercent.compareTo(alertThreshold.multiply(new BigDecimal("100"))) >= 0) {
                    log.info("✅ 用户 {} 达到通知条件，发送通知...", userProduct.getUserId());

                    notificationService.sendPriceDropNotification(
                            userProduct.getUserId(),
                            product.getName(),
                            oldPrice,
                            newPrice
                    );
                    log.info("已为用户 {} 发送降价通知", userProduct.getUserId());
                } else {
                    log.debug("降价幅度未达到用户 {} 的提醒阈值", userProduct.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("触发降价通知失败", e);
        }
    }
}
