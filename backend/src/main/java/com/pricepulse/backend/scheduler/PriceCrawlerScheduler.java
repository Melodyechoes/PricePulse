package com.pricepulse.backend.scheduler;

import com.pricepulse.backend.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PriceCrawlerScheduler {

    @Autowired
    private ProductService productService;

    @Value("${scheduler.price-update.enabled:true}")
    private boolean priceUpdateEnabled;

    @Value("${scheduler.deep-update.enabled:true}")
    private boolean deepUpdateEnabled;

    /**
     * 每 30 分钟执行一次价格更新
     */
    @Scheduled(fixedRate = 1800000) // 30 分钟 = 1800000 毫秒
    public void updatePrices() {
        if (!priceUpdateEnabled) {
            log.debug("定时价格更新任务已禁用，跳过");
            return;
        }

        log.info("开始执行定时价格更新任务...");
        try {
            int updatedCount = productService.updateAllProductsPrice();
            log.info("价格更新完成，共更新 {} 个商品", updatedCount);
        } catch (Exception e) {
            log.error("价格更新任务执行失败", e);
        }
    }

    /**
     * 每天凌晨 2 点执行一次深度更新
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void deepUpdate() {
        if (!deepUpdateEnabled) {
            log.debug("深度价格更新任务已禁用，跳过");
            return;
        }

        log.info("开始执行每日深度价格更新任务...");
        try {
            int updatedCount = productService.deepUpdateAllProducts();
            log.info("深度更新完成，共更新 {} 个商品", updatedCount);
        } catch (Exception e) {
            log.error("深度更新任务执行失败", e);
        }
    }
}
