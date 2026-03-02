package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.UserProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserProductMapper {

    /**
     * 插入关注记录
     */
    @Insert("INSERT INTO user_products(user_id, product_id, target_price, notification_enabled, price_drop_threshold) " +
            "VALUES(#{userId}, #{productId}, #{targetPrice}, #{notificationEnabled}, #{priceDropThreshold})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserProduct userProduct);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM user_products WHERE id = #{id}")
    UserProduct selectById(Long id);

    /**
     * 根据用户ID和商品ID查询
     */
    @Select("SELECT * FROM user_products WHERE user_id = #{userId} AND product_id = #{productId}")
    UserProduct selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据用户ID查询关注列表
     */
    @Select("SELECT up.*, p.name as product_name, p.current_price as product_price, p.image_url as product_image " +
            "FROM user_products up " +
            "LEFT JOIN products p ON up.product_id = p.id " +
            "WHERE up.user_id = #{userId} AND up.notification_enabled = 1 " +
            "ORDER BY up.created_at DESC")
    List<UserProduct> selectByUserId(Long userId);

    /**
     * 根据商品ID查询关注用户
     */
    @Select("SELECT up.*, u.username " +
            "FROM user_products up " +
            "LEFT JOIN users u ON up.user_id = u.id " +
            "WHERE up.product_id = #{productId} AND up.notification_enabled = 1")
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
