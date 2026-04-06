package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.Product;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {


    /**
     * 插入商品
     */
    @Insert("INSERT INTO products(name, url, image_url, platform, platform_id, brand, category, " +
            "current_price, original_price, discount_rate, sales_count, rating, review_count, stock_status) " +
            "VALUES(#{name}, #{url}, #{imageUrl}, #{platform}, #{platformId}, #{brand}, #{category}, " +
            "#{currentPrice}, #{originalPrice}, #{discountRate}, #{salesCount}, #{rating}, #{reviewCount}, #{stockStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    /**
     * 根据 ID 查询商品
     */
    @Select("SELECT * FROM products WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    Product selectById(Long id);

    /**
     * 查询所有商品
     */
    @Select("SELECT * FROM products WHERE status = 1 ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    List<Product> selectAll();

    /**
     * 根据平台 ID 查询商品
     */
    @Select("SELECT * FROM products WHERE platform_id = #{platformId} AND platform = #{platform}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    Product selectByPlatformId(@Param("platformId") String platformId, @Param("platform") String platform);

    /**
     * 更新商品信息
     */
    @Update("UPDATE products SET name = #{name}, url = #{url}, image_url = #{imageUrl}, " +
            "platform = #{platform}, platform_id = #{platformId}, brand = #{brand}, category = #{category}, " +
            "current_price = #{currentPrice}, original_price = #{originalPrice}, discount_rate = #{discountRate}, " +
            "sales_count = #{salesCount}, rating = #{rating}, review_count = #{reviewCount}, " +
            "stock_status = #{stockStatus}, last_checked = NOW(), updated_at = NOW() " +
            "WHERE id = #{id}")
    int update(Product product);

    /**
     * 删除商品
     */
    @Delete("DELETE FROM products WHERE id = #{id}")
    int deleteById(Long id);
    /**
     * 根据分类查询商品
     */
    @Select("SELECT * FROM products WHERE category = #{category} AND status = 1 ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    List<Product> selectByCategory(String category);

    /**
     * 根据品牌查询商品
     */
    @Select("SELECT * FROM products WHERE brand = #{brand} AND status = 1 ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    List<Product> selectByBrand(String brand);

    /**
     * 根据价格范围查询商品
     */
    @Select("SELECT * FROM products WHERE current_price BETWEEN #{minPrice} AND #{maxPrice} " +
            "AND status = 1 ORDER BY current_price ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    List<Product> selectByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 根据关键词搜索商品
     */
    @Select("SELECT * FROM products WHERE (name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR brand LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 " +
            "ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    List<Product> searchByKeyword(String keyword);

    /**
     * 按分类统计商品数量
     */
    @SelectProvider(type = ProductSqlProvider.class, method = "countByCategory")
    List<Map<String, Object>> countByCategory(@Param("productIds") java.util.List<Long> productIds);

    /**
     * 按平台统计商品数量
     */
    @SelectProvider(type = ProductSqlProvider.class, method = "countByPlatform")
    List<Map<String, Object>> countByPlatform(@Param("productIds") java.util.List<Long> productIds);

    /**
     * 多条件组合搜索商品
     */
    @SelectProvider(type = ProductSqlProvider.class, method = "searchWithFilters")
    List<Product> searchWithFilters(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("platform") String platform,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );


    /**
     * 获取所有分类
     */
    @Select("SELECT DISTINCT category FROM products WHERE status = 1 AND category IS NOT NULL ORDER BY category")
    List<String> selectAllCategories();

    /**
     * 获取所有平台
     */
    @Select("SELECT DISTINCT platform FROM products WHERE status = 1 AND platform IS NOT NULL ORDER BY platform")
    List<String> selectAllPlatforms();

    /**
     * 根据商品 ID 查询关注的用户 ID 列表
     */
    @Select("SELECT user_id FROM user_products WHERE product_id = #{productId}")
    List<Long> selectUserIdsByProductId(@Param("productId") Long productId);

    /**
     * 根据 ID 更新商品（简化版，只更新价格相关字段）
     */
    @Update("UPDATE products SET current_price = #{currentPrice}, original_price = #{originalPrice}, " +
            "discount_rate = #{discountRate}, updated_at = NOW() WHERE id = #{id}")
    int updateById(Product product);

    /**
     * 根据状态查询商品
     */
    @Select("SELECT * FROM products WHERE status = #{status} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    List<Product> selectByStatus(@Param("status") Integer status);

    /**
     * 查询所有商品（管理员用，包含所有状态）
     */
    @Select("SELECT * FROM products ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "category", column = "category"),
            @Result(property = "currentPrice", column = "current_price"),
            @Result(property = "originalPrice", column = "original_price"),
            @Result(property = "discountRate", column = "discount_rate"),
            @Result(property = "salesCount", column = "sales_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "reviewCount", column = "review_count"),
            @Result(property = "stockStatus", column = "stock_status"),
            @Result(property = "lastChecked", column = "last_checked"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "status", column = "status")
    })
    List<Product> selectAllProducts();

    /**
     * 更新描述
     */
    @Update("UPDATE products SET description = #{description}, updated_at = NOW() WHERE id = #{id}")
    int updateDescription(@Param("id") Long id, @Param("description") String description);

    /**
     * 更新商品状态
     */
    @Update("UPDATE products SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

