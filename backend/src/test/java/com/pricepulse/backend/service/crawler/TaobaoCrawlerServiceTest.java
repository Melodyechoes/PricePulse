package com.pricepulse.backend.service.crawler;

import com.pricepulse.backend.common.dto.PriceInfo;
import com.pricepulse.backend.service.crawler.impl.TaobaoCrawlerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaobaoCrawlerServiceTest {

    @Autowired
    private TaobaoCrawlerServiceImpl taobaoCrawlerService;

    @Test
    void testSupports() {
        // 测试淘宝链接
        assertThat(taobaoCrawlerService.supports("https://item.taobao.com/item.htm?id=123456")).isTrue();

        // 测试天猫链接
        assertThat(taobaoCrawlerService.supports("https://detail.tmall.com/item.htm?id=789012")).isTrue();

        // 测试非淘宝链接
        assertThat(taobaoCrawlerService.supports("https://item.jd.com/123456.html")).isFalse();
    }

    @Test
    void testCrawlPrice() {
        // 使用真实的商品链接测试（如果有的话）
        String testUrl = "https://detail.tmall.com/item.htm?id=123456789";

        PriceInfo priceInfo = taobaoCrawlerService.crawlPrice(testUrl);

        // 验证返回结果
        assertThat(priceInfo).isNotNull();
        // 注意：实际测试中可能需要处理网络请求失败的情况
    }
}
