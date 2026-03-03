package com.pricepulse.backend.service.crawler;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.impl.JDCrawlerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JDCrawlerServiceTest {

    @Autowired
    private JDCrawlerServiceImpl jdCrawlerService;

    @Test
    void testSupports() {
        // 测试京东链接
        assertThat(jdCrawlerService.supports("https://item.jd.com/123456.html")).isTrue();

        // 测试非京东链接
        assertThat(jdCrawlerService.supports("https://detail.tmall.com/item.htm?id=123")).isFalse();
    }

    @Test
    void testCrawlPrice() {
        String testUrl = "https://item.jd.com/100012345678.html";

        PriceInfo priceInfo = jdCrawlerService.crawlPrice(testUrl);

        assertThat(priceInfo).isNotNull();
    }
}
