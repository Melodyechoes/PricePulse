package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class NotificationService {

    /**
     * 发送降价通知
     */
    public void sendPriceDropNotification(Long userId, Product product, BigDecimal currentPrice) {
        log.info("📢 发送降价通知 - 用户:{}, 商品:{}, 当前价格:{}",
                userId, product.getName(), currentPrice);

        // TODO: 实现具体的通知方式（邮件、短信、站内信等）
        // 这里只是记录日志，实际项目中需要集成消息推送服务
    }

    /**
     * 发送到货通知
     */
    public void sendStockNotification(Long userId, Product product) {
        log.info("📢 发送到货通知 - 用户:{}, 商品:{}", userId, product.getName());

        // TODO: 实现具体的通知方式
    }
}
