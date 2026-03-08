package com.pricepulse.backend.service;

import com.pricepulse.backend.mapper.PriceHistoryMapper;
import com.pricepulse.backend.mapper.ProductMapper;
import com.pricepulse.backend.mapper.UserProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserProductMapper userProductMapper;

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    /**
     * 获取统计数据
     */
    public Map<String, Object> getDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 关注商品数量
        int followedCount = userProductMapper.countByUserId(userId);
        stats.put("followedCount", followedCount);

        // 2. 今日降价商品数量
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int priceDropCount = priceHistoryMapper.countPriceDropsToday(userId, todayStart);
        stats.put("priceDropCount", priceDropCount);

        // 3. 预计省钱金额（所有关注商品的降价总额）
        BigDecimal savedAmount = priceHistoryMapper.calculateTotalSavings(userId);
        stats.put("savedAmount", savedAmount != null ? savedAmount : BigDecimal.ZERO);

        // 4. 未读通知数量（通过 NotificationService 获取）
        // 这里简化处理，实际应该调用 NotificationService
        stats.put("unreadCount", 0);

        return stats;
    }

    /**
     * 获取价格趋势数据
     */
    public List<Map<String, Object>> getPriceTrend(Long userId, Integer days) {
        List<Map<String, Object>> trend = new ArrayList<>();

        // 获取用户关注的商品 ID 列表
        var userProducts = userProductMapper.selectByUserId(userId);
        if (userProducts == null || userProducts.isEmpty()) {
            return trend;
        }

        List<Long> productIds = userProducts.stream()
                .map(up -> up.getProductId())
                .collect(Collectors.toList());

        // 如果产品 ID 列表为空，直接返回
        if (productIds.isEmpty()) {
            return trend;
        }

        // 查询最近 N 天的平均价格
        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();
        return priceHistoryMapper.getAveragePriceByDays(productIds, startDate, days);
    }


    /**
     * 获取分类分布数据
     */
    public List<Map<String, Object>> getCategoryDistribution(Long userId) {
        List<Map<String, Object>> distribution = new ArrayList<>();

        // 获取用户关注的商品
        var userProducts = userProductMapper.selectByUserId(userId);
        if (userProducts == null || userProducts.isEmpty()) {
            return distribution;
        }

        List<Long> productIds = userProducts.stream()
                .map(up -> up.getProductId())
                .collect(Collectors.toList());

        // 如果产品 ID 列表为空，直接返回
        if (productIds.isEmpty()) {
            return distribution;
        }

        // 按分类统计 - 直接返回 List<Map<String, Object>>
        return productMapper.countByCategory(productIds);
    }

    /**
     * 获取降价排行榜
     */
    public List<Map<String, Object>> getPriceDropRanking(Long userId, Integer limit) {
        List<Map<String, Object>> ranking = new ArrayList<>();

        // 获取用户关注的商品
        var userProducts = userProductMapper.selectByUserId(userId);
        if (userProducts == null || userProducts.isEmpty()) {
            return ranking;
        }

        List<Long> productIds = userProducts.stream()
                .map(up -> up.getProductId())
                .collect(Collectors.toList());

        // 如果产品 ID 列表为空，直接返回
        if (productIds.isEmpty()) {
            return ranking;
        }

        // 查询降价数据
        return priceHistoryMapper.getPriceDropRanking(productIds, limit);
    }
}
