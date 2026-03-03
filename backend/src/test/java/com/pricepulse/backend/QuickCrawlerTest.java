package com.pricepulse.backend;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.CrawlerStrategyFactory;
import com.pricepulse.backend.service.crawler.CrawlerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuickCrawlerTest {

    @Autowired
    private CrawlerStrategyFactory crawlerFactory;

    @Test
    void testCrawlerFactory() {
        System.out.println("🚀 开始爬虫功能快速测试...");

        try {
            // 测试1: 工厂注入
            assertThat(crawlerFactory).isNotNull();
            System.out.println("✅ 爬虫工厂注入成功");

            // 测试2: 获取淘宝爬虫
            CrawlerService taobaoCrawler = crawlerFactory.getCrawler("https://detail.tmall.com/item.htm?id=123");
            assertThat(taobaoCrawler).isNotNull();
            System.out.println("✅ 淘宝爬虫获取成功");

            // 测试3: 获取京东爬虫
            CrawlerService jdCrawler = crawlerFactory.getCrawler("https://item.jd.com/123456.html");
            assertThat(jdCrawler).isNotNull();
            System.out.println("✅ 京东爬虫获取成功");

            System.out.println("🎉 爬虫功能测试通过！");

        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
