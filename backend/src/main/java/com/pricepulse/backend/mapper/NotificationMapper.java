package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.NotificationEntity;
import com.pricepulse.backend.common.entity.Product;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper {

    /**
     * 插入通知
     */
    @Insert("INSERT INTO notifications(user_id, message, type, is_read, related_product_id, created_at, updated_at) " +
            "VALUES(#{userId}, #{message}, #{type}, #{isRead}, #{relatedProductId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NotificationEntity notification);

    /**
     * 根据用户 ID 查询通知列表
     */
    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 100")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "message", column = "message"),
            @Result(property = "type", column = "type"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "relatedProductId", column = "related_product_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<NotificationEntity> selectByUserId(Long userId);

    /**
     * 根据 ID 查询通知
     */
    @Select("SELECT * FROM notifications WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "message", column = "message"),
            @Result(property = "type", column = "type"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "relatedProductId", column = "related_product_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    NotificationEntity selectById(Long id);

    /**
     * 更新通知为已读
     */
    @Update("UPDATE notifications SET is_read = 1, updated_at = NOW() WHERE id = #{id}")
    int updateIsRead(Long id, int isRead);

    /**
     * 批量标记已读
     */
    @Update("UPDATE notifications SET is_read = 1, updated_at = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int updateAllAsRead(Long userId);

    /**
     * 删除通知
     */
    @Delete("DELETE FROM notifications WHERE id = #{id}")
    int deleteById(Long id);
    /**
     * 统计未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(Long userId);

    /**
     * 根据用户 ID 和时间统计通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND created_at >= #{since}")
    int countByUserIdAndTime(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * 按类型统计通知数量
     */
    @Select("SELECT type, COUNT(*) as count FROM notifications WHERE user_id = #{userId} GROUP BY type")
    List<Map<String, Object>> countByType(Long userId);

    // ... existing code ...


    /**
     * 统计用户的通知总数（包含已读和未读）
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId}")
    int countByUserId(Long userId);

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
     * 根据 ID 更新商品（简化版，只更新价格相关字段）
     */
    @Update("UPDATE products SET current_price = #{currentPrice}, original_price = #{originalPrice}, " +
            "discount_rate = #{discountRate}, updated_at = NOW() WHERE id = #{id}")
    int updateById(Product product);


}
