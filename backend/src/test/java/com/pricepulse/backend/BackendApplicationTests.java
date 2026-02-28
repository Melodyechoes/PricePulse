package com.pricepulse.backend;

import com.pricepulse.backend.service.DatabaseInitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseInitService databaseInitService;

    @Test
    void contextLoads() {
        // 简单的上下文加载测试
    }

    @Test
    void testDatabaseConnection() {
        // 测试数据库连接
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertThat(result).isEqualTo(1);
            System.out.println("✅ 数据库连接测试成功");
        } catch (Exception e) {
            System.err.println("❌ 数据库连接测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    void testDatabaseInitialization() {
        System.out.println("开始数据库初始化测试...");

        try {
            // 先检查数据库连接
            testDatabaseConnection();

            // 执行数据库初始化
            databaseInitService.initializeDatabase();

            System.out.println("数据库初始化完成，开始验证表结构...");

            // 验证关键表是否存在
            validateRequiredTables();

            // 显示所有创建的表
            System.out.println("✅ 成功创建的表: " + databaseInitService.getAllTables());
            System.out.println("✅ 数据库初始化测试通过");

        } catch (Exception e) {
            System.err.println("❌ 数据库初始化测试失败: " + e.getMessage());
            System.err.println("当前数据库中的表: " + databaseInitService.getAllTables());
            throw e;
        }
    }

    private void validateRequiredTables() {
        String[] requiredTables = {"users", "products", "price_history", "user_products"};

        for (String tableName : requiredTables) {
            boolean exists = databaseInitService.tableExists(tableName);
            assertThat(exists)
                    .withFailMessage("表 %s 未创建成功", tableName)
                    .isTrue();
            System.out.println("✓ 表 " + tableName + " 创建成功");
        }
    }
}
