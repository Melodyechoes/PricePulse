package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.websocket.NotificationWebSocketHandler;
import com.pricepulse.backend.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*") // 允许跨域
public class TestController {

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Autowired
    private ProductMapper productMapper;
    /**
     * 测试 WebSocket 推送
     */
    @GetMapping("/ws/push")
    public String testWebSocketPush(@RequestParam Long userId) {
        try {
            String message = "{\"type\":\"PRICE_DROP\",\"message\":\"📉 这是测试通知！商品价格已更新。\",\"time\":\"" + LocalDateTime.now() + "\"}";

            webSocketHandler.sendNotification(userId, message);

            return "已向用户 " + userId + " 发送测试通知";
        } catch (Exception e) {
            return "发送失败：" + e.getMessage();
        }
    }

    /**
     * 检查 WebSocket 连接状态
     */
    @GetMapping("/ws/status")
    public String getWebSocketStatus() {
        return "WebSocket 服务运行中";
    }

    /**
     * 创建测试商品数据
     */
    @GetMapping("/products/create")
    public String createTestProducts() {
        try {
            // 创建测试商品 1 - 淘宝手机
            Product phone1 = new Product();
            phone1.setName("Apple iPhone 15 Pro Max");
            phone1.setUrl("https://item.taobao.com/item.htm?id=123456");
            phone1.setImageUrl("https://img.alicdn.com/imgextra/i1/O1CN01test.jpg");
            phone1.setPlatform("TMALL");
            phone1.setPlatformId("123456");
            phone1.setBrand("Apple");
            phone1.setCategory("digital");
            phone1.setCurrentPrice(new BigDecimal("8999.00"));
            phone1.setOriginalPrice(new BigDecimal("9999.00"));
            phone1.setDiscountRate(new BigDecimal("10.00"));
            phone1.setSalesCount(1000);
            phone1.setRating(new BigDecimal("4.8"));
            phone1.setReviewCount(500);
            phone1.setStockStatus(1);
            phone1.setStatus(1);
            productMapper.insert(phone1);

            // 创建测试商品 2 - 京东手机
            Product phone2 = new Product();
            phone2.setName("华为 Mate 60 Pro");
            phone2.setUrl("https://item.jd.com/123456.html");
            phone2.setImageUrl("https://img14.360buyimg.com/n1/test.jpg");
            phone2.setPlatform("JD");
            phone2.setPlatformId("123456");
            phone2.setBrand("华为");
            phone2.setCategory("digital");
            phone2.setCurrentPrice(new BigDecimal("6999.00"));
            phone2.setOriginalPrice(new BigDecimal("7999.00"));
            phone2.setDiscountRate(new BigDecimal("12.00"));
            phone2.setSalesCount(2000);
            phone2.setRating(new BigDecimal("4.9"));
            phone2.setReviewCount(1000);
            phone2.setStockStatus(1);
            phone2.setStatus(1);
            productMapper.insert(phone2);

            // 创建测试商品 3 - 淘宝服装
            Product clothing1 = new Product();
            clothing1.setName("优衣库男装羽绒服");
            clothing1.setUrl("https://item.taobao.com/item.htm?id=789012");
            clothing1.setImageUrl("https://img.alicdn.com/imgextra/i2/O1CN01clothing.jpg");
            clothing1.setPlatform("TMALL");
            clothing1.setPlatformId("789012");
            clothing1.setBrand("优衣库");
            clothing1.setCategory("服装");
            clothing1.setCurrentPrice(new BigDecimal("399.00"));
            clothing1.setOriginalPrice(new BigDecimal("599.00"));
            clothing1.setDiscountRate(new BigDecimal("33.00"));
            clothing1.setSalesCount(5000);
            clothing1.setRating(new BigDecimal("4.7"));
            clothing1.setReviewCount(3000);
            clothing1.setStockStatus(1);
            clothing1.setStatus(1);
            productMapper.insert(clothing1);

            // 创建测试商品 4 - 拼多多数码
            Product digital1 = new Product();
            digital1.setName("小米手环 8 Pro");
            digital1.setUrl("https://mobile.yangkeduo.com/goods.html?goods_id=345678");
            digital1.setImageUrl("https://t00img.yangkeduo.com/goods/images/test.jpg");
            digital1.setPlatform("PDD");
            digital1.setPlatformId("345678");
            digital1.setBrand("小米");
            digital1.setCategory("digital");
            digital1.setCurrentPrice(new BigDecimal("299.00"));
            digital1.setOriginalPrice(new BigDecimal("399.00"));
            digital1.setDiscountRate(new BigDecimal("25.00"));
            digital1.setSalesCount(10000);
            digital1.setRating(new BigDecimal("4.6"));
            digital1.setReviewCount(8000);
            digital1.setStockStatus(1);
            digital1.setStatus(1);
            productMapper.insert(digital1);

            return "✅ 已创建 4 个测试商品数据：<br/>" +
                    "1. Apple iPhone 15 Pro Max (TMALL) - ¥8999<br/>" +
                    "2. 华为 Mate 60 Pro (JD) - ¥6999<br/>" +
                    "3. 优衣库男装羽绒服 (TMALL) - ¥399<br/>" +
                    "4. 小米手环 8 Pro (PDD) - ¥299";
        } catch (Exception e) {
            return "创建失败：" + e.getMessage();
        }
    }
}
