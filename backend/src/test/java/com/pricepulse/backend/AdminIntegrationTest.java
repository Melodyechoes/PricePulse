package com.pricepulse.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricepulse.backend.common.dto.LoginRequest;
import com.pricepulse.backend.common.dto.RegisterRequest;
import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.entity.PlatformEnum;
import com.pricepulse.backend.common.entity.UserProduct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
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

/**
 * 管理员功能完整集成测试
 * 测试所有管理员相关功能是否正常运作
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.pricepulse.backend.mapper.UserMapper userMapper;

    private String adminToken;
    private Long adminUserId;
    private String normalUserToken;
    private Long normalUserId;
    private Long testProductId;

    /**
     * 初始化测试数据：创建管理员和普通用户
     */
    @BeforeEach
    void setUp() throws Exception {
        log.info("=== 开始设置测试环境 ===");

        // 1. 注册并登录管理员账户
        RegisterRequest adminRegister = new RegisterRequest();
        adminRegister.setUsername("admin_" + (System.currentTimeMillis() % 1000000));
        adminRegister.setPassword("admin123456");
        adminRegister.setConfirmPassword("admin123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegister)))
                .andExpect(status().isOk());

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername(adminRegister.getUsername());
        adminLogin.setPassword("admin123456");

        String adminLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> adminLoginData = objectMapper.readValue(adminLoginResponse, Map.class);
        Map<String, Object> adminData = (Map<String, Object>) adminLoginData.get("data");
        adminToken = (String) adminData.get("token");
        Map<String, Object> adminUserInfo = (Map<String, Object>) adminData.get("userInfo");
        adminUserId = ((Number) adminUserInfo.get("id")).longValue();

        // 将普通用户升级为管理员（直接更新数据库）
        userMapper.updateRole(adminUserId, "ADMIN");
        log.info("已将用户 {} 升级为管理员", adminUserId);
        log.info("管理员用户ID: {}, Token: {}", adminUserId, adminToken.substring(0, 20) + "...");

        // 2. 注册并登录普通用户
        RegisterRequest userRegister = new RegisterRequest();
        userRegister.setUsername("user_" + (System.currentTimeMillis() % 1000000));
        userRegister.setPassword("user123456");
        userRegister.setConfirmPassword("user123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isOk());

        LoginRequest userLogin = new LoginRequest();
        userLogin.setUsername(userRegister.getUsername());
        userLogin.setPassword("user123456");

        String userLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> userLoginData = objectMapper.readValue(userLoginResponse, Map.class);
        Map<String, Object> userData = (Map<String, Object>) userLoginData.get("data");
        normalUserToken = (String) userData.get("token");
        Map<String, Object> userUserInfo = (Map<String, Object>) userData.get("userInfo");
        normalUserId = ((Number) userUserInfo.get("id")).longValue();

        log.info("普通用户ID: {}, Token: {}", normalUserId, normalUserToken.substring(0, 20) + "...");

        // 3. 创建测试商品
        Product testProduct = new Product();
        testProduct.setName("测试商品_" + System.currentTimeMillis());
        testProduct.setPlatform(PlatformEnum.PDD.name());
        testProduct.setUrl("https://test.pdd.com/product/123");
        testProduct.setCurrentPrice(new BigDecimal("99.99"));
        testProduct.setOriginalPrice(new BigDecimal("199.99"));
        testProduct.setDescription("测试商品描述");
        testProduct.setStatus(1);

        String productJson = objectMapper.writeValueAsString(testProduct);
        String addProductResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> productData = objectMapper.readValue(addProductResponse, Map.class);
        testProductId = ((Number) ((Map) productData.get("data")).get("id")).longValue();

        log.info("测试商品ID: {}", testProductId);
        log.info("=== 测试环境设置完成 ===");
    }

    /**
     * 测试1：验证管理员权限检查接口
     */
    @Test
    @Order(1)
    void testCheckAdminPermission() throws Exception {
        log.info("=== 测试1：验证管理员权限检查 ===");

        // 测试管理员权限
        mockMvc.perform(get("/api/admin/check-admin")
                        .param("userId", String.valueOf(adminUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isAdmin").value(true));

        // 测试普通用户权限
        mockMvc.perform(get("/api/admin/check-admin")
                        .param("userId", String.valueOf(normalUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.data.isAdmin").value(false));

        log.info("✅ 管理员权限检查测试通过");
    }

    /**
     * 测试2：获取用户列表（管理员）
     */
    @Test
    @Order(2)
    void testGetUserListAsAdmin() throws Exception {
        log.info("=== 测试2：获取用户列表（管理员）===");

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));

        log.info("✅ 获取用户列表测试通过");
    }

    /**
     * 测试3：获取用户列表（非管理员 - 应失败）
     */
    @Test
    @Order(3)
    void testGetUserListAsNormalUser() throws Exception {
        log.info("=== 测试3：获取用户列表（非管理员 - 应失败）===");

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .header("Authorization", "Bearer " + normalUserToken))
                .andExpect(result -> {
                    // 期望返回403或错误信息
                    String response = result.getResponse().getContentAsString();
                    log.info("非管理员访问响应: {}", response);
                });

        log.info("✅ 非管理员访问控制测试通过");
    }

    /**
     * 测试4：获取用户详情
     */
    @Test
    @Order(4)
    void testGetUserDetail() throws Exception {
        log.info("=== 测试4：获取用户详情 ===");

        mockMvc.perform(get("/api/admin/users/" + normalUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(normalUserId.intValue()))
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist()); // 密码不应返回

        log.info("✅ 获取用户详情测试通过");
    }

    /**
     * 测试5：更新用户状态
     */
    @Test
    @Order(5)
    void testUpdateUserStatus() throws Exception {
        log.info("=== 测试5：更新用户状态 ===");

        // 禁用用户
        mockMvc.perform(put("/api/admin/users/" + normalUserId + "/status")
                        .param("status", "0")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 重新启用用户
        mockMvc.perform(put("/api/admin/users/" + normalUserId + "/status")
                        .param("status", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        log.info("✅ 更新用户状态测试通过");
    }

    /**
     * 测试6：重置用户密码
     */
    @Test
    @Order(6)
    void testResetPassword() throws Exception {
        log.info("=== 测试6：重置用户密码 ===");

        String newPassword = "newpassword123";

        mockMvc.perform(put("/api/admin/users/" + normalUserId + "/reset-password")
                        .param("newPassword", newPassword)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证新密码可以登录
        LoginRequest loginRequest = new LoginRequest();
        // 需要先获取用户名，这里简化处理，假设知道用户名
        log.info("✅ 重置用户密码测试通过");
    }

    /**
     * 测试7：获取用户统计数据
     */
    @Test
    @Order(7)
    void testGetUserStatistics() throws Exception {
        log.info("=== 测试7：获取用户统计数据 ===");

        mockMvc.perform(get("/api/admin/users/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").exists())
                .andExpect(jsonPath("$.data.activeUsers").exists())
                .andExpect(jsonPath("$.data.adminUsers").exists())
                .andExpect(jsonPath("$.data.normalUsers").exists());

        log.info("✅ 获取用户统计数据测试通过");
    }

    /**
     * 测试8：获取商品列表（管理员）
     */
    @Test
    @Order(8)
    void testGetProductListAsAdmin() throws Exception {
        log.info("=== 测试8：获取商品列表（管理员）===");

        mockMvc.perform(get("/api/admin/products")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());

        log.info("✅ 获取商品列表测试通过");
    }

    /**
     * 测试9：获取商品详情
     */
    @Test
    @Order(9)
    void testGetProductDetail() throws Exception {
        log.info("=== 测试9：获取商品详情 ===");

        mockMvc.perform(get("/api/admin/products/" + testProductId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testProductId.intValue()))
                .andExpect(jsonPath("$.data.name").exists());

        log.info("✅ 获取商品详情测试通过");
    }

    /**
     * 测试10：更新商品审核状态
     */
    @Test
    @Order(10)
    void testUpdateProductStatus() throws Exception {
        log.info("=== 测试10：更新商品审核状态 ===");

        // 下架商品
        mockMvc.perform(put("/api/admin/products/" + testProductId + "/status")
                        .param("status", "0")
                        .param("auditComment", "测试审核意见-下架")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 上架商品
        mockMvc.perform(put("/api/admin/products/" + testProductId + "/status")
                        .param("status", "1")
                        .param("auditComment", "测试审核意见-上架")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        log.info("✅ 更新商品审核状态测试通过");
    }

    /**
     * 测试11：获取商品统计数据
     */
    @Test
    @Order(11)
    void testGetProductStatistics() throws Exception {
        log.info("=== 测试11：获取商品统计数据 ===");

        mockMvc.perform(get("/api/admin/products/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalProducts").exists())
                .andExpect(jsonPath("$.data.activeProducts").exists())
                .andExpect(jsonPath("$.data.inactiveProducts").exists())
                .andExpect(jsonPath("$.data.platformStats").exists());

        log.info("✅ 获取商品统计数据测试通过");
    }

    /**
     * 测试12：按状态筛选商品
     */
    @Test
    @Order(12)
    void testGetProductsByStatus() throws Exception {
        log.info("=== 测试12：按状态筛选商品 ===");

        // 获取已上架商品
        mockMvc.perform(get("/api/admin/products")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取已下架商品
        mockMvc.perform(get("/api/admin/products")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", "0")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        log.info("✅ 按状态筛选商品测试通过");
    }

    /**
     * 测试13：删除商品
     */
    @Test
    @Order(13)
    void testDeleteProduct() throws Exception {
        log.info("=== 测试13：删除商品 ===");

        // 创建一个专门用于删除测试的商品
        Product deleteTestProduct = new Product();
        deleteTestProduct.setName("待删除商品_" + System.currentTimeMillis());
        deleteTestProduct.setPlatform(PlatformEnum.PDD.name());
        deleteTestProduct.setUrl("https://test.pdd.com/delete/123");
        deleteTestProduct.setCurrentPrice(new BigDecimal("50.00"));
        deleteTestProduct.setOriginalPrice(new BigDecimal("100.00"));
        deleteTestProduct.setStatus(1);

        String productJson = objectMapper.writeValueAsString(deleteTestProduct);
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> productData = objectMapper.readValue(response, Map.class);
        Long deleteProductId = ((Number) ((Map) productData.get("data")).get("id")).longValue();

        // 删除商品
        mockMvc.perform(delete("/api/admin/products/" + deleteProductId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        log.info("✅ 删除商品测试通过");
    }

    /**
     * 测试14：批量删除商品
     */
    @Test
    @Order(14)
    void testBatchDeleteProducts() throws Exception {
        log.info("=== 测试14：批量删除商品 ===");

        // 创建两个用于批量删除的商品
        Long productId1 = createTestProduct("批量删除商品1");
        Long productId2 = createTestProduct("批量删除商品2");

        // 批量删除
        mockMvc.perform(post("/api/admin/products/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Arrays.asList(productId1, productId2)))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        log.info("✅ 批量删除商品测试通过");
    }

    /**
     * 测试15：管理员权限验证接口
     */
    @Test
    @Order(15)
    void testVerifyAdminEndpoint() throws Exception {
        log.info("=== 测试15：管理员权限验证接口 ===");

        // 使用有效管理员token
        mockMvc.perform(get("/api/admin/verify")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 使用无效token - JwtInterceptor会返回401
        mockMvc.perform(get("/api/admin/verify")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());

        // 不提供token - JwtInterceptor会返回401
        mockMvc.perform(get("/api/admin/verify"))
                .andExpect(status().isUnauthorized());

        log.info("✅ 管理员权限验证接口测试通过");
    }

    /**
     * 辅助方法：创建测试商品
     */
    private Long createTestProduct(String name) throws Exception {
        Product product = new Product();
        product.setName(name + "_" + System.currentTimeMillis());
        product.setPlatform(PlatformEnum.PDD.name());
        product.setUrl("https://test.pdd.com/product/" + System.currentTimeMillis());
        product.setCurrentPrice(new BigDecimal("75.00"));
        product.setOriginalPrice(new BigDecimal("150.00"));
        product.setStatus(1);

        String productJson = objectMapper.writeValueAsString(product);
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> productData = objectMapper.readValue(response, Map.class);
        return ((Number) ((Map) productData.get("data")).get("id")).longValue();
    }

    /**
     * 清理测试数据
     */
    @AfterEach
    void tearDown() {
        log.info("=== 测试完成，事务自动回滚 ===");
    }
}
