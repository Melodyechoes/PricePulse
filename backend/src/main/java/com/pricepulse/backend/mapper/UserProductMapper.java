package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.common.entity.UserProductWithProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserProductMapper {
    /**
     * 定义结果映射
     */
    @Results(id = "userProductResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "targetPrice", column = "target_price"),
            @Result(property = "notificationEnabled", column = "notification_enabled"),
            @Result(property = "priceDropThreshold", column = "price_drop_threshold"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })

    /**
     * 插入关注记录
     */
    @Insert("INSERT INTO user_products(user_id, product_id, target_price, notification_enabled, price_drop_threshold) " +
            "VALUES(#{userId}, #{productId}, #{targetPrice}, #{notificationEnabled}, #{priceDropThreshold})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserProduct userProduct);

    /**
     * 根据 ID 查询
     */
    @Select("SELECT * FROM user_products WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "targetPrice", column = "target_price"),
            @Result(property = "notificationEnabled", column = "notification_enabled"),
            @Result(property = "priceDropThreshold", column = "price_drop_threshold"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    UserProduct selectById(Long id);

    /**
     * 根据用户 ID 和商品 ID 查询
     */
    @Select("SELECT * FROM user_products WHERE user_id = #{userId} AND product_id = #{productId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "targetPrice", column = "target_price"),
            @Result(property = "notificationEnabled", column = "notification_enabled"),
            @Result(property = "priceDropThreshold", column = "price_drop_threshold"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    UserProduct selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据用户 ID 查询关注列表（关联商品信息）
     */
    @Select("SELECT up.*, " +
            "p.name as product_name, " +
            "p.current_price as product_current_price, " +
            "p.image_url as product_image_url " +
            "FROM user_products up " +
            "LEFT JOIN products p ON up.product_id = p.id " +
            "WHERE up.user_id = #{userId} " +
            "ORDER BY up.created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "targetPrice", column = "target_price"),
            @Result(property = "notificationEnabled", column = "notification_enabled"),
            @Result(property = "priceDropThreshold", column = "price_drop_threshold"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "productCurrentPrice", column = "product_current_price"),
            @Result(property = "productImageUrl", column = "product_image_url")
    })
    List<UserProductWithProduct> selectByUserIdWithProductInfo(Long userId);

    /**
     * 根据商品 ID 查询关注用户
     */
    @Select("SELECT * FROM user_products WHERE product_id = #{productId} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "targetPrice", column = "target_price"),
            @Result(property = "notificationEnabled", column = "notification_enabled"),
            @Result(property = "priceDropThreshold", column = "price_drop_threshold"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<UserProduct> selectByProductId(Long productId);
    /**
     * 更新关注设置
     */
    @Update("UPDATE user_products SET target_price = #{targetPrice}, notification_enabled = #{notificationEnabled}, " +
            "price_drop_threshold = #{priceDropThreshold}, updated_at = NOW() WHERE id = #{id}")
    int update(UserProduct userProduct);

    /**
     * 根据用户ID和商品ID删除
     */
    @Delete("DELETE FROM user_products WHERE user_id = #{userId} AND product_id = #{productId}")
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 统计用户关注商品数量
     */
    @Select("SELECT COUNT(*) FROM user_products WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    /**
     * 统计商品被关注数量
     */
    @Select("SELECT COUNT(*) FROM user_products WHERE product_id = #{productId}")
    int countByProductId(Long productId);
}
