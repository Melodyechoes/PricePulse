package com.pricepulse.backend.service.crawler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 爬虫策略工厂集成测试
 */
@SpringBootTest
@DisplayName("爬虫策略工厂集成测试")
class CrawlerStrategyFactoryIntegrationTest {

    @Autowired
    private CrawlerStrategyFactory factory;

    @Test
    @DisplayName("测试获取京东爬虫服务")
    void testGetCrawler_JD() {
        CrawlerService crawler = factory.getCrawler("https://item.jd.com/123456.html");

        assertThat(crawler).isNotNull();
        assertThat(crawler.getPlatform()).isEqualTo("jd");
    }

    @Test
    @DisplayName("测试获取淘宝爬虫服务")
    void testGetCrawler_Taobao() {
        CrawlerService crawler = factory.getCrawler("https://item.taobao.com/item.htm?id=123456");

        assertThat(crawler).isNotNull();
        assertThat(crawler.getPlatform()).isEqualTo("taobao");
    }

    @Test
    @DisplayName("测试获取天猫爬虫服务")
    void testGetCrawler_Tmall() {
        CrawlerService crawler = factory.getCrawler("https://detail.tmall.com/item.htm?id=123456");

        assertThat(crawler).isNotNull();
        assertThat(crawler.getPlatform()).isEqualTo("taobao");
    }

    @Test
    @DisplayName("测试获取拼多多爬虫服务")
    void testGetCrawler_Pdd() {
        CrawlerService crawler = factory.getCrawler("https://mobile.yangkeduo.com/goods.html?goods_id=123456");

        assertThat(crawler).isNotNull();
        assertThat(crawler.getPlatform()).isEqualTo("pdd");
    }

    @Test
    @DisplayName("测试获取平台标识 - 京东")
    void testGetPlatform_JD() {
        String platform = factory.getPlatform("https://item.jd.com/123456.html");
        assertThat(platform).isEqualTo("jd");
    }

    @Test
    @DisplayName("测试获取平台标识 - 淘宝")
    void testGetPlatform_Taobao() {
        String platform = factory.getPlatform("https://item.taobao.com/item.htm?id=123456");
        assertThat(platform).isEqualTo("taobao");
    }

    @Test
    @DisplayName("测试获取平台标识 - 拼多多")
    void testGetPlatform_Pdd() {
        String platform = factory.getPlatform("https://mobile.yangkeduo.com/goods.html?goods_id=123456");
        assertThat(platform).isEqualTo("pdd");
    }

    @Test
    @DisplayName("测试不支持的平台")
    void testGetCrawler_UnsupportedPlatform() {
        assertThatThrownBy(() -> factory.getCrawler("https://example.com"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("暂不支持该平台的商品链接");
    }
}
