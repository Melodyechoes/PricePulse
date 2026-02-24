// 包声明：指定这个类所在的包位置
package com.zlb.pricepulse.repository;

// 导入所需的类

import com.zlb.pricepulse.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * @Repository 注解：
 * 1. 告诉Spring这个类是数据访问层组件
 * 2. Spring会自动扫描并创建这个类的实例（称为Bean）
 * 3. 其他类可以通过@Autowired注入这个Repository
 */
@Repository
public class ProductRepository {

    /**
     * @Autowired 注解：
     * 1. 自动注入JdbcTemplate实例
     * 2. Spring会在启动时自动创建JdbcTemplate并赋值给这个字段
     * 3. JdbcTemplate是Spring对JDBC的封装，简化数据库操作
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存商品到数据库
     *
     * @param product 要保存的商品对象
     * @return 返回插入后生成的主键ID
     */
    public Long save(Product product) {
        // SQL插入语句，?是占位符，防止SQL注入
        String sql = "INSERT INTO products (name, url, image_url) VALUES (?, ?, ?)";

        /**
         * KeyHolder作用：
         * 1. 用于获取数据库自动生成的主键
         * 2. 比如MySQL的AUTO_INCREMENT字段
         */
        KeyHolder keyHolder = new GeneratedKeyHolder();

        /**
         * jdbcTemplate.update方法：
         * 1. 第一个参数：PreparedStatementCreator，用于创建预编译语句
         * 2. 第二个参数：KeyHolder，用于接收生成的主键
         *
         * 这里使用Lambda表达式简化代码
         */
        jdbcTemplate.update(connection -> {
            // 创建预编译语句，Statement.RETURN_GENERATED_KEYS表示需要返回生成的主键
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            // 设置SQL参数，注意索引从1开始
            ps.setString(1, product.getName());      // 第一个?替换为商品名称
            ps.setString(2, product.getUrl());       // 第二个?替换为商品URL
            ps.setString(3, product.getImageUrl());  // 第三个?替换为图片URL
            return ps;
        }, keyHolder);

        // 从KeyHolder中获取生成的主键值
        return keyHolder.getKey().longValue();
    }

    /**
     * 根据ID查找商品
     *
     * @param id 商品ID
     * @return Optional包装的商品对象，避免返回null
     */
    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try {
            /**
             * queryForObject方法：
             * 1. 查询单个对象
             * 2. 第二个参数：RowMapper，将查询结果转换为Java对象
             * 3. BeanPropertyRowMapper自动映射列名到对象属性（下划线转驼峰）
             * 4. 第三个参数：SQL参数值
             */
            Product product = jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(Product.class), id);
            // 如果查询成功，用Optional包装返回
            return Optional.ofNullable(product);
        } catch (Exception e) {
            // 如果查询失败（如记录不存在），返回空的Optional
            return Optional.empty();
        }
    }

    /**
     * 查找所有商品
     *
     * @return 商品列表
     */
    public List<Product> findAll() {
        String sql = "SELECT * FROM products ORDER BY created_at DESC";
        /**
         * query方法：
         * 1. 查询多个对象，返回List
         * 2. BeanPropertyRowMapper自动完成结果集到对象的转换
         */
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class));
    }

    /**
     * 根据URL查找商品（用于去重检查）
     *
     * @param url 商品URL
     * @return Optional包装的商品对象
     */
    public Optional<Product> findByUrl(String url) {
        String sql = "SELECT * FROM products WHERE url = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql,
                    new BeanPropertyRowMapper<>(Product.class), url);
            return Optional.ofNullable(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}