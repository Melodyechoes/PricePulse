package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.Product;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

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
     * 根据ID查询商品
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
     * 根据平台ID查询商品（用于去重）
     */
    @Select("SELECT * FROM products WHERE platform_id = #{platformId} AND platform = #{platform}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "url", column = "url"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "platformId", column = "platform_id"),  // 确保这个映射正确
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
     * 删除商品（软删除）
     */
    @Update("UPDATE products SET status = 0, updated_at = NOW() WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据分类查询商品
     */
    @Select("SELECT * FROM products WHERE category = #{category} AND status = 1 ORDER BY created_at DESC")
    List<Product> selectByCategory(String category);

    /**
     * 根据品牌查询商品
     */
    @Select("SELECT * FROM products WHERE brand = #{brand} AND status = 1 ORDER BY created_at DESC")
    List<Product> selectByBrand(String brand);

    /**
     * 根据价格范围查询商品
     */
    @Select("SELECT * FROM products WHERE current_price BETWEEN #{minPrice} AND #{maxPrice} " +
            "AND status = 1 ORDER BY current_price ASC")
    List<Product> selectByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
}
