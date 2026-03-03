package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.PriceHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PriceHistoryMapper {

    /**
     * 插入价格历史记录
     */
    @Insert("INSERT INTO price_history(product_id, price, original_price, discount_rate, currency, checked_at, source) " +
            "VALUES(#{productId}, #{price}, #{originalPrice}, #{discountRate}, #{currency}, #{checkedAt}, #{source})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PriceHistory priceHistory);

    /**
     * 根据ID查询价格历史
     */
    @Select("SELECT * FROM price_history WHERE id = #{id}")
    PriceHistory selectById(Long id);

    /**
     * 根据商品ID查询价格历史（按时间倒序）
     */
    @Select("SELECT * FROM price_history WHERE product_id = #{productId} ORDER BY checked_at DESC")
    List<PriceHistory> selectByProductId(Long productId);

    /**
     * 根据商品ID查询最新的价格记录
     */
    @Select("SELECT * FROM price_history WHERE product_id = #{productId} ORDER BY checked_at DESC LIMIT 1")
    PriceHistory selectLatestByProductId(Long productId);

    /**
     * 查询指定时间范围内的价格历史
     */
    @Select("SELECT * FROM price_history WHERE product_id = #{productId} AND checked_at BETWEEN #{startTime} AND #{endTime} ORDER BY checked_at DESC")
    List<PriceHistory> selectByTimeRange(@Param("productId") Long productId,
                                         @Param("startTime") java.time.LocalDateTime startTime,
                                         @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 统计商品价格记录数量
     */
    @Select("SELECT COUNT(*) FROM price_history WHERE product_id = #{productId}")
    int countByProductId(Long productId);

    /**
     * 删除指定天前的价格历史记录
     */
    @Delete("DELETE FROM price_history WHERE checked_at < #{date}")
    int deleteOlderThan(java.time.LocalDateTime date);
}
