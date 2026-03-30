package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.PriceHistory;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 统计今日降价商品数量
     */
    @Select("SELECT COUNT(DISTINCT ph.product_id) " +
            "FROM price_history ph " +
            "INNER JOIN user_products up ON ph.product_id = up.product_id " +
            "WHERE up.user_id = #{userId} " +
            "AND ph.checked_at >= #{todayStart} " +
            "AND ph.price < (SELECT MAX(price) FROM price_history WHERE product_id = ph.product_id AND checked_at < ph.checked_at)")
    int countPriceDropsToday(@Param("userId") Long userId, @Param("todayStart") LocalDateTime todayStart);

    /**
     * 计算总节省金额
     */
    @Select("SELECT IFNULL(SUM((p.original_price - p.current_price) * IFNULL(p.sales_count, 1)), 0) " +
            "FROM products p " +
            "INNER JOIN user_products up ON p.id = up.product_id " +
            "WHERE up.user_id = #{userId} " +
            "AND p.original_price IS NOT NULL " +
            "AND p.current_price < p.original_price")
    BigDecimal calculateTotalSavings(@Param("userId") Long userId);


    /**
     * 获取最近 N 天的平均价格（使用 SQL Provider）
     */
    @SelectProvider(type = PriceHistorySqlProvider.class, method = "getAveragePriceByDays")
    List<Map<String, Object>> getAveragePriceByDays(
            @Param("productIds") List<Long> productIds,
            @Param("startDate") LocalDateTime startDate,
            @Param("days") Integer days);

    /**
     * 获取每个商品在最近 N 天的每日价格（用于多商品对比）
     */
    @SelectProvider(type = PriceHistorySqlProvider.class, method = "getDailyPricesByProducts")
    List<Map<String, Object>> getDailyPricesByProducts(
            @Param("productIds") List<Long> productIds,
            @Param("startDate") LocalDateTime startDate);



    /**
     * 按分类统计商品数量（使用 SQL Provider）
     */
    @SelectProvider(type = ProductSqlProvider.class, method = "countByCategory")
    Map<String, Integer> countByCategory(@Param("productIds") List<Long> productIds);

    /**
     * 获取降价排行榜（使用 SQL Provider）
     */
    @SelectProvider(type = PriceHistorySqlProvider.class, method = "getPriceDropRanking")
    List<Map<String, Object>> getPriceDropRanking(
            @Param("productIds") List<Long> productIds,
            @Param("limit") Integer limit);

    /**
     * 获取价格波动率排行（使用 SQL Provider）
     */
    @SelectProvider(type = PriceHistorySqlProvider.class, method = "getVolatilityRanking")
    List<Map<String, Object>> getVolatilityRanking(
            @Param("productIds") List<Long> productIds,
            @Param("limit") Integer limit);

    /**
     * 根据商品 ID 删除价格历史
     */
    @Delete("DELETE FROM price_history WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

}
