package com.pricepulse.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DatabaseInitService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 初始化数据库表结构
     */
    public void initializeDatabase() {
        try {
            log.info("开始初始化数据库...");

            // 读取SQL文件
            ClassPathResource resource = new ClassPathResource("sql/init.sql");
            String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            // 使用简单可靠的执行方式
            executeSqlDirectly(sql);

            log.info("数据库初始化完成！");

        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 直接执行SQL文件
     */
    private void executeSqlDirectly(String sql) {
        // 移除注释行和空行
        String[] lines = sql.split("\n");
        StringBuilder cleanSql = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();
            // 跳过注释行和空行
            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("--")) {
                cleanSql.append(line).append("\n");
            }
        }

        String finalSql = cleanSql.toString();
        log.debug("清理后的SQL长度: {} 字符", finalSql.length());

        // 按分号分割并执行
        String[] statements = finalSql.split(";");

        int executedCount = 0;
        int errorCount = 0;

        for (String statement : statements) {
            String trimmedStatement = statement.trim();
            if (!trimmedStatement.isEmpty()) {
                try {
                    log.debug("执行SQL语句: {}",
                            trimmedStatement.length() > 100 ?
                                    trimmedStatement.substring(0, 100) + "..." :
                                    trimmedStatement);

                    jdbcTemplate.execute(trimmedStatement);
                    executedCount++;
                    log.debug("✓ 执行成功");
                } catch (Exception e) {
                    errorCount++;
                    log.warn("✗ 执行失败: {} - 错误: {}",
                            trimmedStatement.substring(0, Math.min(50, trimmedStatement.length())),
                            e.getMessage());

                    // 对于可忽略的错误继续执行
                    if (isIgnorableError(e, trimmedStatement)) {
                        log.info("忽略可接受的错误，继续执行");
                        continue;
                    }

                    // 对于关键错误抛出异常
                    if (isCriticalError(e, trimmedStatement)) {
                        log.error("关键SQL执行失败，停止初始化");
                        throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
                    }
                }
            }
        }

        log.info("SQL执行统计 - 成功: {}, 失败: {}", executedCount, errorCount);
    }

    /**
     * 判断是否是可以忽略的错误
     */
    private boolean isIgnorableError(Exception e, String statement) {
        String errorMsg = e.getMessage().toLowerCase();
        String lowerStatement = statement.toLowerCase();

        // DROP语句如果表不存在可以忽略
        if (lowerStatement.startsWith("drop") &&
                (errorMsg.contains("doesn't exist") || errorMsg.contains("not found"))) {
            return true;
        }

        // CREATE INDEX如果索引已存在可以忽略
        if (lowerStatement.startsWith("create index") &&
                errorMsg.contains("already exists")) {
            return true;
        }

        // INSERT如果记录已存在可以忽略
        if (lowerStatement.startsWith("insert") &&
                errorMsg.contains("duplicate entry")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否是关键错误
     */
    private boolean isCriticalError(Exception e, String statement) {
        String errorMsg = e.getMessage().toLowerCase();
        String lowerStatement = statement.toLowerCase();

        // CREATE TABLE语句失败通常是关键错误
        if (lowerStatement.startsWith("create table")) {
            // 但如果是表已存在，则可以忽略
            if (errorMsg.contains("already exists")) {
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            boolean exists = count != null && count > 0;
            log.debug("表 {} 存在性检查: {}", tableName, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查表存在性失败: {}", tableName, e);
            return false;
        }
    }

    /**
     * 获取所有表名
     */
    public List<String> getAllTables() {
        try {
            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            log.error("获取表列表失败", e);
            return Arrays.asList();
        }
    }
}
