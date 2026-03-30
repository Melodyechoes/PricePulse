package com.pricepulse.backend.service.crawler;

import com.pricepulse.backend.common.dto.PriceInfo;

import java.math.BigDecimal;

/**
 * 商品爬虫服务接口
 */
public interface CrawlerService {

    /**
     * 抓取商品价格信息
     * @param url 商品链接
     * @return 价格信息
     */
    PriceInfo crawlPrice(String url);

    /**
     * 判断是否支持该平台
     * @param url 商品链接
     * @return 是否支持
     */
    boolean supports(String url);

    /**
     * 获取平台标识
     * @return 平台标识（jd/taobao/pdd）
     */
    String getPlatform();
}