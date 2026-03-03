package com.pricepulse.backend.service.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrawlerStrategyFactory {

    @Autowired
    private List<CrawlerService> crawlerServices;

    /**
     * 根据URL获取对应的爬虫服务
     */
    public CrawlerService getCrawler(String url) {
        for (CrawlerService crawler : crawlerServices) {
            if (crawler.supports(url)) {
                return crawler;
            }
        }
        throw new UnsupportedOperationException("暂不支持该平台的商品链接: " + url);
    }
}
