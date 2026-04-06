package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.mapper.PriceHistoryMapper;
import com.pricepulse.backend.mapper.ProductMapper;
import com.pricepulse.backend.mapper.UserProductMapper;
import com.pricepulse.backend.service.crawler.CrawlerStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品管理服务
 * <p>
 * 提供商品的增删改查、价格更新、搜索过滤等核心功能
 *
 * @author PricePulse Team
 * @since 2026-04-06
 */
@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CrawlerStrategyFactory crawlerFactory;

    @Autowired
    private UserProductMapper userProductMapper;

    /**
     * 添加商品
     * <p>
     * 1. 验证商品参数
     * 2. 检查是否已存在相同商品（根据platformId）
     * 3. 设置默认值（status=1, stockStatus=1）
     * 4. 插入数据库
     *
     * @param product 商品信息
     * @return 保存后的商品，包含生成的ID
     * @throws BusinessException 当参数无效或商品已存在时抛出
     */
    @Transactional
    public Product addProduct(Product product) {
        // 参数验证
        validateProduct(product);

        // 检查是否已存在相同商品
        if (StringUtils.hasText(product.getPlatformId())) {
            Product existingProduct = productMapper.selectByPlatformId(
                    product.getPlatformId(), product.getPlatform());
            if (existingProduct != null) {
                throw new BusinessException("该商品已存在");
            }
        }

        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getStockStatus() == null) {
            product.setStockStatus(1);
        }

        // 插入商品
        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException("商品添加失败");
        }

        log.info("商品添加成功, ID: {}, name: {}", product.getId(), product.getName());
        return product;
    }


    /**
     * 根据ID查询商品
     *
     * @param id 商品ID
     * @return 商品详细信息
     * @throws BusinessException 当ID无效或商品不存在时抛出
     */
    public Product getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("商品ID不能为空");
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        return product;
    }

    /**
     * 获取所有商品
     */
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    /**
     * 更新商品信息
     */
    @Transactional
    public Product updateProduct(Product product) {
        // 验证ID
        if (product.getId() == null || product.getId() <= 0) {
            throw new BusinessException("商品ID不能为空");
        }

        // 验证商品是否存在
        Product existingProduct = productMapper.selectById(product.getId());
        if (existingProduct == null) {
            throw new BusinessException("商品不存在");
        }

        // 参数验证
        validateProduct(product);

        // 更新商品
        int result = productMapper.update(product);
        if (result <= 0) {
            throw new BusinessException("商品更新失败");
        }

        log.info("商品更新成功，ID: {}", product.getId());
        return product;
    }

    /**
     * 删除商品
     * <p>
     * 级联删除：
     * 1. 删除用户关注记录（user_products表）
     * 2. 删除价格历史记录（price_history表）
     * 3. 删除商品本身（products表）
     *
     * @param id 商品ID
     * @throws BusinessException 当ID无效或商品不存在时抛出
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("商品 ID 不能为空");
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否有人关注，如果有，先删除关注记录
        List<UserProduct> userProducts = userProductMapper.selectByProductId(id);
        if (!userProducts.isEmpty()) {
            log.info("商品 {} 被 {} 个用户关注，将同步删除关注记录", product.getName(), userProducts.size());
            for (UserProduct userProduct : userProducts) {
                userProductMapper.deleteByUserIdAndProductId(userProduct.getUserId(), id);
            }
        }

        // 删除价格历史记录
        priceHistoryMapper.deleteByProductId(id);

        // 删除商品本身
        int result = productMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException("商品删除失败");
        }

        log.info("商品删除成功, ID: {}, name: {}", id, product.getName());
    }

    /**
     * 根据分类查询商品
     */
    public List<Product> getProductsByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            throw new BusinessException("分类不能为空");
        }
        return productMapper.selectByCategory(category);
    }

    /**
     * 根据品牌查询商品
     */
    public List<Product> getProductsByBrand(String brand) {
        if (!StringUtils.hasText(brand)) {
            throw new BusinessException("品牌不能为空");
        }
        return productMapper.selectByBrand(brand);
    }

    /**
     * 根据价格范围查询商品
     */
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new BusinessException("价格范围不能为空");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("价格不能为负数");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("最小价格不能大于最大价格");
        }
        return productMapper.selectByPriceRange(minPrice, maxPrice);
    }


    /**
     * 验证商品参数
     */
    private void validateProduct(Product product) {
        if (!StringUtils.hasText(product.getName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (!StringUtils.hasText(product.getUrl())) {
            throw new BusinessException("商品链接不能为空");
        }
        if (!StringUtils.hasText(product.getPlatform())) {
            throw new BusinessException("商品平台不能为空");
        }
        if (product.getCurrentPrice() == null) {
            throw new BusinessException("商品价格不能为空");
        }
        if (product.getCurrentPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格必须大于 0");
        }
    }

    /**
     * 根据关键词搜索商品
     */
    public List<Product> searchByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException("搜索关键词不能为空");
        }
        return productMapper.searchByKeyword(keyword);
    }


    /**
     * 多条件组合搜索
     */
    public List<Product> searchWithFilters(Map<String, Object> params) {
        String keyword = (String) params.get("keyword");
        String category = (String) params.get("category");
        String platform = (String) params.get("platform");
        BigDecimal minPrice = (BigDecimal) params.get("minPrice");
        BigDecimal maxPrice = (BigDecimal) params.get("maxPrice");

        // 如果只有关键词，直接使用关键词搜索
        if (keyword != null && category == null && platform == null &&
                minPrice == null && maxPrice == null) {
            return searchByKeyword(keyword);
        }

        // 多条件查询
        return productMapper.searchWithFilters(
                keyword,
                category,
                platform,
                minPrice,
                maxPrice
        );
    }

    /**
     * 获取所有可用的筛选条件
     */
    public Map<String, Object> getAvailableFilters() {
        Map<String, Object> filters = new HashMap<>();

        // 获取所有分类
        List<String> categories = productMapper.selectAllCategories();
        filters.put("categories", categories);

        // 获取所有平台
        List<String> platforms = productMapper.selectAllPlatforms();
        filters.put("platforms", platforms);

        return filters;
    }

    /**
     * 更新所有商品价格（定时任务）
     */
    public int updateAllProductsPrice() {
        List<Product> allProducts = productMapper.selectAll();
        int updatedCount = 0;

        for (Product product : allProducts) {
            try {
                // 使用真实爬虫获取价格
                BigDecimal newPrice = crawlRealPrice(product);

                if (newPrice != null && !newPrice.equals(product.getCurrentPrice())) {
                    // 检查是否降价
                    boolean isPriceDrop = newPrice.compareTo(product.getCurrentPrice()) < 0;

                    // 更新商品价格
                    product.setCurrentPrice(newPrice);
                    int result = productMapper.update(product);

                    if (result > 0) {
                        updatedCount++;

                        // 记录价格历史
                        recordPriceHistory(product);

                        // 如果降价，发送通知
                        if (isPriceDrop) {
                            sendPriceDropNotifications(product);
                        }

                        log.info("商品 {} 价格更新：{} -> {}", product.getName(),
                                product.getCurrentPrice(), newPrice);
                    }
                }
            } catch (Exception e) {
                log.error("更新商品 {} 价格失败", product.getName(), e);
            }
        }

        return updatedCount;
    }


    /**
     * 深度更新所有商品（每日执行）
     */
    public int deepUpdateAllProducts() {
        List<Product> allProducts = productMapper.selectAll();
        int updatedCount = 0;

        for (Product product : allProducts) {
            try {
                // 使用真实爬虫获取价格
                BigDecimal newPrice = crawlRealPrice(product);
                Integer newSalesCount = simulateSalesUpdate(product.getSalesCount());

                if (newPrice != null) {
                    product.setCurrentPrice(newPrice);
                    product.setSalesCount(newSalesCount);
                    product.setLastChecked(LocalDateTime.now());

                    int result = productMapper.update(product);

                    if (result > 0) {
                        updatedCount++;
                        recordPriceHistory(product);
                    }
                }
            } catch (Exception e) {
                log.error("深度更新商品 {} 失败", product.getName(), e);
            }
        }

        return updatedCount;
    }

    /**
     * 爬取真实商品价格
     */
    private BigDecimal crawlRealPrice(Product product) {
        try {
            String url = product.getUrl();
            if (url == null || url.isEmpty()) {
                log.warn("商品 {} 缺少 URL，跳过", product.getName());
                return product.getCurrentPrice();
            }

            // 获取对应的爬虫服务
            com.pricepulse.backend.service.crawler.CrawlerService crawler =
                    crawlerFactory.getCrawler(url);

            // 爬取价格
            com.pricepulse.backend.common.dto.PriceInfo priceInfo = crawler.crawlPrice(url);

            if (priceInfo != null && priceInfo.getCurrentPrice() != null) {
                log.info("成功爬取商品 {} 价格：{}", product.getName(), priceInfo.getCurrentPrice());
                return priceInfo.getCurrentPrice();
            } else {
                log.warn("爬取商品 {} 价格失败，返回空值", product.getName());
                return product.getCurrentPrice();
            }
        } catch (Exception e) {
            log.error("爬取商品 {} 价格异常", product.getName(), e);
            return product.getCurrentPrice();
        }
    }

    /**
     * 记录价格历史
     */
    private void recordPriceHistory(Product product) {
        try {
            com.pricepulse.backend.common.entity.PriceHistory history =
                    new com.pricepulse.backend.common.entity.PriceHistory();
            history.setProductId(product.getId());
            history.setPrice(product.getCurrentPrice());
            history.setOriginalPrice(product.getOriginalPrice());
            history.setDiscountRate(product.getDiscountRate());
            history.setCurrency("CNY");
            history.setCheckedAt(LocalDateTime.now());
            history.setSource("SCHEDULER");

            priceHistoryMapper.insert(history);
        } catch (Exception e) {
            log.error("记录价格历史失败", e);
        }
    }

    /**
     * 发送降价通知
     */
    private void sendPriceDropNotifications(Product product) {
        try {
            // 获取关注该商品的所有用户
            List<Long> userIds = productMapper.selectUserIdsByProductId(product.getId());

            for (Long userId : userIds) {
                notificationService.sendPriceDropNotification(
                        userId,
                        product.getName(),
                        product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getCurrentPrice(),
                        product.getCurrentPrice()
                );
            }
        } catch (Exception e) {
            log.error("发送降价通知失败", e);
        }
    }

    /**
     * 模拟销量更新
     */
    private Integer simulateSalesUpdate(Integer currentSales) {
        if (currentSales == null) return 0;
        // 模拟销量增长
        int increase = (int) (Math.random() * 100);
        return currentSales + increase;
    }

    /**
     * 判断用户是否已关注某商品
     */
    public boolean isProductFollowedByUser(Long productId, Long userId) {
        if (productId == null || userId == null) {
            return false;
        }

        Integer count = userProductMapper.countByUserIdAndProductId(userId, productId);
        return count != null && count > 0;
    }
}
