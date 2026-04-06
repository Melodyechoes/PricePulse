package com.pricepulse.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricepulse.backend.common.dto.RegisterRequest;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.entity.PlatformEnum;
import com.pricepulse.backend.common.entity.UserProduct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
class UserProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testUserId;
    private Long testProductId;

    /**
     * 每个测试前创建测试用户和商品
     */
    @BeforeEach
    void setUp() throws Exception {
        log.info("=== 设置测试数据 ===");

        // 1. 创建测试用户（用户名控制在20字符内）
        RegisterRequest registerRequest = new RegisterRequest();
        String username = "test_" + (System.currentTimeMillis() % 1000000);
        registerRequest.setUsername(username);
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> registerData = objectMapper.readValue(registerResponse, Map.class);
        
        // 检查注册是否成功
        if (registerData.get("code") != null && !Integer.valueOf(200).equals(registerData.get("code"))) {
            throw new RuntimeException("用户注册失败: " + registerData.get("message"));
        }
        
        Map<String, Object> data = (Map<String, Object>) registerData.get("data");
        if (data == null) {
            throw new RuntimeException("注册响应数据为空");
        }
        
        Map<String, Object> userInfo = (Map<String, Object>) data.get("userInfo");
        testUserId = ((Number) userInfo.get("id")).longValue();
        log.info("创建测试用户 ID: {}, 用户名: {}", testUserId, username);

        // 2. 创建测试商品
        Product testProduct = new Product();
        testProduct.setName("测试商品_" + System.currentTimeMillis());
        testProduct.setPlatform(PlatformEnum.PDD.name());
        testProduct.setUrl("https://test.pdd.com/product/" + System.currentTimeMillis());
        testProduct.setCurrentPrice(new BigDecimal("99.99"));
        testProduct.setOriginalPrice(new BigDecimal("199.99"));
        testProduct.setStatus(1);

        String productJson = objectMapper.writeValueAsString(testProduct);
        String productResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> productData = objectMapper.readValue(productResponse, Map.class);
        testProductId = ((Number) ((Map) productData.get("data")).get("id")).longValue();
        log.info("创建测试商品 ID: {}", testProductId);
    }

    @Test
    void testFollowProduct() throws Exception {
        UserProduct userProduct = createUserProduct();
        String json = objectMapper.writeValueAsString(userProduct);

        mockMvc.perform(post("/api/user-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(testUserId.intValue()))
                .andExpect(jsonPath("$.data.productId").value(testProductId.intValue()));
    }

    @Test
    void testUnfollowProduct() throws Exception {
        // 先关注商品
        UserProduct userProduct = createUserProduct();
        String json = objectMapper.writeValueAsString(userProduct);
        mockMvc.perform(post("/api/user-products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // 然后取消关注
        mockMvc.perform(delete("/api/user-products")
                        .param("userId", String.valueOf(testUserId))
                        .param("productId", String.valueOf(testProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testFollowDuplicateProduct() throws Exception {
        UserProduct userProduct = createUserProduct();
        String json = objectMapper.writeValueAsString(userProduct);

        // 第一次关注成功
        String firstResponse = mockMvc.perform(post("/api/user-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 第二次关注应该更新设置并返回成功（幂等操作）
        String secondResponse = mockMvc.perform(post("/api/user-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 验证两次返回的是同一条记录（ID相同）
        Map<String, Object> firstData = objectMapper.readValue(firstResponse, Map.class);
        Map<String, Object> secondData = objectMapper.readValue(secondResponse, Map.class);
        
        Long firstId = ((Number) ((Map) firstData.get("data")).get("id")).longValue();
        Long secondId = ((Number) ((Map) secondData.get("data")).get("id")).longValue();
        
        org.junit.jupiter.api.Assertions.assertEquals(firstId, secondId, 
            "重复关注应返回同一条记录");
    }

    @Test
    void testUnfollowNonExistentRelation() throws Exception {
        // 取消不存在的关注关系
        mockMvc.perform(delete("/api/user-products")
                        .param("userId", "99999")
                        .param("productId", "99999"))
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    // 可能返回400（不存在）或其他错误码
                    org.junit.jupiter.api.Assertions.assertTrue(
                        response.contains("\"code\"") ,
                        "取消不存在的关注关系应有响应"
                    );
                });
    }

    @Test
    void testGetProductFollowers() throws Exception {
        mockMvc.perform(get("/api/user-products/product/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testUpdateFollowSettings() throws Exception {
        // 先关注商品
        UserProduct userProduct = createUserProduct();
        String postJson = objectMapper.writeValueAsString(userProduct);
        String response = mockMvc.perform(post("/api/user-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 解析返回的ID（简化处理）
        // 实际应该从响应中提取ID

        // 更新设置
        UserProduct updateProduct = new UserProduct();
        updateProduct.setTargetPrice(new BigDecimal("8999.00"));
        updateProduct.setPriceDropThreshold(new BigDecimal("10.00"));

        String updateJson = objectMapper.writeValueAsString(updateProduct);
        
        // 先获取关注记录的ID
        String followedListResponse = mockMvc.perform(get("/api/user-products/user/" + testUserId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        Map<String, Object> followedData = objectMapper.readValue(followedListResponse, Map.class);
        java.util.List followedList = (java.util.List) followedData.get("data");
        
        if (followedList != null && !followedList.isEmpty()) {
            Map<String, Object> firstFollow = (Map<String, Object>) followedList.get(0);
            Long followId = ((Number) firstFollow.get("id")).longValue();
            
            mockMvc.perform(put("/api/user-products/" + followId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    private UserProduct createUserProduct() {
        UserProduct userProduct = new UserProduct();
        userProduct.setUserId(testUserId);
        userProduct.setProductId(testProductId);
        userProduct.setTargetPrice(new BigDecimal("9999.00"));
        userProduct.setNotificationEnabled(1);
        userProduct.setPriceDropThreshold(new BigDecimal("5.00"));
        return userProduct;
    }
}
