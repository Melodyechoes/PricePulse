package com.pricepulse.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricepulse.backend.common.entity.UserProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFollowProduct() throws Exception {
        UserProduct userProduct = createUserProduct();
        String json = objectMapper.writeValueAsString(userProduct);

        mockMvc.perform(post("/api/user-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.productId").value(1));
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
                        .param("userId", "1")
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetUserFollowedProducts() throws Exception {
        mockMvc.perform(get("/api/user-products/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetProductFollowers() throws Exception {
        mockMvc.perform(get("/api/user-products/product/1"))
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
        mockMvc.perform(put("/api/user-products/1")  // 使用已知ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private UserProduct createUserProduct() {
        UserProduct userProduct = new UserProduct();
        userProduct.setUserId(1L);
        userProduct.setProductId(1L);
        userProduct.setTargetPrice(new BigDecimal("9999.00"));
        userProduct.setNotificationEnabled(1);
        userProduct.setPriceDropThreshold(new BigDecimal("5.00"));
        return userProduct;
    }
}
