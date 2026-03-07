package com.pricepulse.backend.service;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.common.entity.PriceHistory;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.mapper.PriceHistoryMapper;
import com.pricepulse.backend.mapper.ProductMapper;
import com.pricepulse.backend.mapper.UserProductMapper;
import com.pricepulse.backend.service.crawler.CrawlerStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PriceCheckService {

    @Autowired
    private CrawlerStrategyFactory crawlerFactory;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    @Autowired
    private UserProductMapper userProductMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * 检查单个商品价格
     */
    @Transactional
    public PriceInfo checkProductPrice(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        try {
            // 使用对应的爬虫抓取价格
            var crawler = crawlerFactory.getCrawler(product.getUrl());
            PriceInfo priceInfo = crawler.crawlPrice(product.getUrl());

            if (priceInfo.getCurrentPrice() != null) {
                // 保存价格历史
                savePriceHistory(product, priceInfo);

                // 检查是否需要发送降价通知
                checkPriceDropAndNotify(product, priceInfo);

                // 更新商品当前价格
                updateProductPrice(product, priceInfo);
            }

            return priceInfo;

        } catch (Exception e) {
            log.error("检查商品价格失败，商品 ID: {}", productId, e);
            return null;
        }
    }

    /**
     * 批量检查所有商品价格
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void checkAllProducts() {
        log.info("开始批量检查商品价格...");

        List<Product> products = productMapper.selectAll();
        int successCount = 0;
        int failCount = 0;

        for (Product product : products) {
            try {
                PriceInfo priceInfo = checkProductPrice(product.getId());
                if (priceInfo != null && priceInfo.getCurrentPrice() != null) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("检查商品{}价格失败", product.getId(), e);
            }
        }

        log.info("批量价格检查完成 - 成功：{}, 失败：{}", successCount, failCount);
    }

    /**
     * 保存价格历史
     */
    private void savePriceHistory(Product product, PriceInfo priceInfo) {
        PriceHistory history = new PriceHistory();
        history.setProductId(product.getId());
        history.setPrice(priceInfo.getCurrentPrice());
        history.setOriginalPrice(priceInfo.getOriginalPrice());
        history.setDiscountRate(priceInfo.getDiscountRate());
        history.setCurrency("CNY");
        history.setCheckedAt(LocalDateTime.now());
        history.setSource("AUTO");

        priceHistoryMapper.insert(history);
        log.debug("保存价格历史 - 商品{}, 价格：{}", product.getId(), priceInfo.getCurrentPrice());
    }

    /**
     * 检查降价并发送通知
     */
    private void checkPriceDropAndNotify(Product product, PriceInfo priceInfo) {
        // 获取关注该商品的用户
        List<UserProduct> userProducts = userProductMapper.selectByProductId(product.getId());

        if (userProducts == null || userProducts.isEmpty()) {
            return;
        }

        for (UserProduct userProduct : userProducts) {
            if (shouldNotify(userProduct, product, priceInfo)) {
                notificationService.sendPriceDropNotification(
                        userProduct.getUserId(),
                        product.getName(),
                        product.getCurrentPrice() != null ? product.getCurrentPrice() : BigDecimal.ZERO,
                        priceInfo.getCurrentPrice() != null ? priceInfo.getCurrentPrice() : BigDecimal.ZERO
                );
            }
        }
    }
    /**
     * 判断是否应该发送通知
     */
    private boolean shouldNotify(UserProduct userProduct, Product product, PriceInfo priceInfo) {
        // 如果用户关闭了通知，则不发送
        if (userProduct.getNotificationEnabled() == null || userProduct.getNotificationEnabled() == 0) {
            return false;
        }

        // 如果当前价格低于用户的期望价格，发送通知
        if (userProduct.getTargetPrice() != null &&
                priceInfo.getCurrentPrice().compareTo(userProduct.getTargetPrice()) <= 0) {
            return true;
        }

        // 如果降价幅度超过用户设置的阈值，发送通知
        if (userProduct.getPriceDropThreshold() != null &&
                product.getCurrentPrice() != null) {
            BigDecimal priceDropPercent = calculatePriceDropPercent(product.getCurrentPrice(), priceInfo.getCurrentPrice());
            if (priceDropPercent.compareTo(userProduct.getPriceDropThreshold()) >= 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 计算降价百分比
     */
    private BigDecimal calculatePriceDropPercent(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || newPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return oldPrice.subtract(newPrice)
                .divide(oldPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 更新商品当前价格
     */
    private void updateProductPrice(Product product, PriceInfo priceInfo) {
        product.setCurrentPrice(priceInfo.getCurrentPrice());
        product.setOriginalPrice(priceInfo.getOriginalPrice());
        product.setDiscountRate(priceInfo.getDiscountRate());
        product.setLastChecked(LocalDateTime.now());

        productMapper.update(product);
    }
}
