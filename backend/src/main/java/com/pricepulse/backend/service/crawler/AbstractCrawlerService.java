package com.pricepulse.backend.service.crawler;

import com.pricepulse.backend.common.dto.PriceInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 爬虫服务抽象基类
 */
@Slf4j
public abstract class AbstractCrawlerService implements CrawlerService {

    @Override
    public PriceInfo crawlPrice(String url) {
        try {
            log.info("【{}爬虫】开始抓取商品价格：{}", getPlatformName(), url);

            String productId = extractProductId(url);
            log.info("提取到商品 ID: {}", productId);

            PriceInfo priceInfo = doCrawlPrice(productId, url);

            log.info("【{}爬虫】价格抓取完成：原价 {} -> 现价 {}, 折扣率 {}%",
                    getPlatformName(),
                    priceInfo.getOriginalPrice(),
                    priceInfo.getCurrentPrice(),
                    priceInfo.getDiscountRate());

            return priceInfo;

        } catch (Exception e) {
            log.error("【{}爬虫】抓取失败：{}", getPlatformName(), url, e);
            return PriceInfo.builder()
                    .errorMessage("抓取失败：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 执行实际的价格抓取逻辑
     */
    protected abstract PriceInfo doCrawlPrice(String productId, String url);

    /**
     * 获取平台名称（用于日志）
     */
    protected abstract String getPlatformName();

    /**
     * 从 URL 中提取商品 ID
     */
    protected abstract String extractProductId(String url);
}
