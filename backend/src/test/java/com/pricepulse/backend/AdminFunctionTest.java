package com.pricepulse.backend;

import com.pricepulse.backend.common.entity.User;
import com.pricepulse.backend.service.AdminUserService;
import com.pricepulse.backend.service.AdminProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 管理员功能测试
 */
@SpringBootTest
@Slf4j
@Transactional
public class AdminFunctionTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminProductService adminProductService;

    /**
     * 测试用户管理功能
     */
    @Test
    public void testUserManagement() {
        log.info("===== 测试用户管理功能 =====");
        
        // 1. 获取用户列表
        Map<String, Object> users = adminUserService.getAllUsers(1, 10);
        assertThat(users).isNotNull();
        assertThat(users.get("list")).isNotNull();
        assertThat(users.get("total")).isNotNull();
        log.info("用户总数：{}", users.get("total"));
        log.info("用户列表大小：{}", ((java.util.List) users.get("list")).size());
        
        // 2. 获取用户统计数据
        Map<String, Object> stats = adminUserService.getUserStatistics();
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalUsers")).isNotNull();
        assertThat(stats.get("activeUsers")).isNotNull();
        assertThat(stats.get("adminUsers")).isNotNull();
        log.info("用户统计：{}", stats);
    }

    /**
     * 测试商品审核功能
     */
    @Test
    public void testProductAudit() {
        log.info("===== 测试商品审核功能 =====");
        
        // 1. 获取商品列表
        Map<String, Object> products = adminProductService.getAllProducts(1, 10, null);
        assertThat(products).isNotNull();
        assertThat(products.get("list")).isNotNull();
        assertThat(products.get("total")).isNotNull();
        log.info("商品总数：{}", products.get("total"));
        log.info("商品列表大小：{}", ((java.util.List) products.get("list")).size());
        
        // 2. 获取商品统计数据
        Map<String, Object> stats = adminProductService.getProductStatistics();
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalProducts")).isNotNull();
        assertThat(stats.get("activeProducts")).isNotNull();
        assertThat(stats.get("platformStats")).isNotNull();
        log.info("商品统计：{}", stats);
    }

    /**
     * 测试更新商品状态
     */
    @Test
    public void testUpdateProductStatus() {
        log.info("===== 测试更新商品审核状态 =====");
        
        // 先获取一个商品ID
        Map<String, Object> products = adminProductService.getAllProducts(1, 1, null);
        java.util.List productList = (java.util.List) products.get("list");
        
        if (productList != null && !productList.isEmpty()) {
            // 使用动态获取的商品 ID
            Object firstProduct = productList.get(0);
            Long productId;
            if (firstProduct instanceof com.pricepulse.backend.common.entity.Product) {
                productId = ((com.pricepulse.backend.common.entity.Product) firstProduct).getId();
            } else {
                log.warn("无法获取商品ID，跳过测试");
                return;
            }
            
            Integer status = 1; // 1=上架，0=下架
            String comment = "测试审核意见";
            
            try {
                adminProductService.updateProductStatus(productId, status, comment);
                log.info("✅ 商品审核状态更新成功");
            } catch (Exception e) {
                log.error("❌ 商品审核状态更新失败：{}", e.getMessage());
                throw e;
            }
        } else {
            log.warn("没有商品可测试");
        }
    }

    /**
     * 测试重置用户密码
     */
    @Test
    public void testResetPassword() {
        log.info("===== 测试重置用户密码 =====");
        
        // 先获取一个用户ID
        Map<String, Object> users = adminUserService.getAllUsers(1, 10);
        java.util.List userList = (java.util.List) users.get("list");
        
        if (userList != null && !userList.isEmpty()) {
            Object firstUser = userList.get(0);
            Long userId;
            if (firstUser instanceof User) {
                userId = ((User) firstUser).getId();
                String role = ((User) firstUser).getRole();
                
                // 不能重置管理员密码
                if ("ADMIN".equals(role)) {
                    log.warn("第一个用户是管理员，跳过密码重置测试");
                    return;
                }
            } else {
                log.warn("无法获取用户ID，跳过测试");
                return;
            }
            
            String newPassword = "test123456";
            
            try {
                adminUserService.resetPassword(userId, newPassword);
                log.info("✅ 密码重置成功");
                
                // 验证用户可以登录（简化处理）
                User user = adminUserService.getUserById(userId);
                assertThat(user).isNotNull();
                log.info("✅ 用户信息验证通过：{}", user.getUsername());
            } catch (Exception e) {
                log.error("❌ 密码重置失败：{}", e.getMessage());
                throw e;
            }
        } else {
            log.warn("没有用户可测试");
        }
    }

    /**
     * 测试获取用户详情
     */
    @Test
    public void testGetUserDetail() {
        log.info("===== 测试获取用户详情 =====");
        
        Map<String, Object> users = adminUserService.getAllUsers(1, 1);
        java.util.List userList = (java.util.List) users.get("list");
        
        if (userList != null && !userList.isEmpty()) {
            Object firstUser = userList.get(0);
            if (firstUser instanceof User) {
                Long userId = ((User) firstUser).getId();
                
                User user = adminUserService.getUserById(userId);
                assertThat(user).isNotNull();
                assertThat(user.getPassword()).isNull(); // 密码不应返回
                log.info("✅ 获取用户详情成功：{}", user.getUsername());
            }
        }
    }

    /**
     * 测试获取商品详情
     */
    @Test
    public void testGetProductDetail() {
        log.info("===== 测试获取商品详情 =====");
        
        Map<String, Object> products = adminProductService.getAllProducts(1, 1, null);
        java.util.List productList = (java.util.List) products.get("list");
        
        if (productList != null && !productList.isEmpty()) {
            Object firstProduct = productList.get(0);
            if (firstProduct instanceof com.pricepulse.backend.common.entity.Product) {
                Long productId = ((com.pricepulse.backend.common.entity.Product) firstProduct).getId();
                
                com.pricepulse.backend.common.entity.Product product = adminProductService.getProductById(productId);
                assertThat(product).isNotNull();
                log.info("✅ 获取商品详情成功：{}", product.getName());
            }
        }
    }
}
