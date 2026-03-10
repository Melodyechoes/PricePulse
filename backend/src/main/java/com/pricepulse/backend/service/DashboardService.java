package com.pricepulse.backend.service;


import com.pricepulse.backend.mapper.PriceHistoryMapper;
import com.pricepulse.backend.mapper.ProductMapper;
import com.pricepulse.backend.mapper.UserProductMapper;
import com.pricepulse.backend.service.cache.DashboardCacheService;
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

    @Autowired
    private DashboardCacheService cacheService;

    /**
     * 获取统计数据
     */
    public Map<String, Object> getDashboardStats(Long userId) {
        // 1. 先尝试从缓存获取
        Object cached = cacheService.getStats(userId);
        if (cached != null) {
            log.info("命中 Dashboard 统计数据缓存，userId={}", userId);
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) cached;
            return stats;
        }

        log.info("未命中缓存，查询数据库，userId={}", userId);

        // 2. 缓存未命中，查询数据库
        Map<String, Object> stats = new HashMap<>();

        // 关注商品数量
        int followedCount = userProductMapper.countByUserId(userId);
        stats.put("followedCount", followedCount);

        // 今日降价商品数量
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int priceDropCount = priceHistoryMapper.countPriceDropsToday(userId, todayStart);
        stats.put("priceDropCount", priceDropCount);

        // 预计省钱金额
        BigDecimal savedAmount = priceHistoryMapper.calculateTotalSavings(userId);
        stats.put("savedAmount", savedAmount != null ? savedAmount : BigDecimal.ZERO);

        // 未读通知数量
        stats.put("unreadCount", 0);

        // 3. 写入缓存
        cacheService.setStats(userId, stats);

        return stats;
    }

    /**
     * 获取价格趋势数据
     */
    public List<Map<String, Object>> getPriceTrend(Long userId, Integer days) {
        // 1. 先尝试从缓存获取
        Object cached = cacheService.getPriceTrend(userId, days);
        if (cached != null) {
            log.info("命中价格趋势缓存，userId={}, days={}", userId, days);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> trend = (List<Map<String, Object>>) cached;
            return trend;
        }

        log.info("未命中价格趋势缓存，userId={}, days={}", userId, days);

        // 2. 缓存未命中，查询数据库
        List<Map<String, Object>> trend = new ArrayList<>();

        var userProducts = userProductMapper.selectByUserId(userId);
        if (userProducts == null || userProducts.isEmpty()) {
            return trend;
        }

        List<Long> productIds = userProducts.stream()
                .map(up -> up.getProductId())
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return trend;
        }

        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();
        trend = priceHistoryMapper.getAveragePriceByDays(productIds, startDate, days);

        // 3. 写入缓存
        cacheService.setPriceTrend(userId, days, trend);

        return trend;
    }


    /**
     * 获取分类分布数据
     */
    public List<Map<String, Object>> getCategoryDistribution(Long userId) {
        // 1. 先尝试从缓存获取
        Object cached = cacheService.getCategoryDistribution(userId);
        if (cached != null) {
            log.info("命中分类分布缓存，userId={}", userId);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> distribution = (List<Map<String, Object>>) cached;
            return distribution;
        }

        log.info("未命中分类分布缓存，userId={}", userId);

        // 2. 缓存未命中，查询数据库
        List<Map<String, Object>> distribution = new ArrayList<>();

        var userProducts = userProductMapper.selectByUserId(userId);
        if (userProducts == null || userProducts.isEmpty()) {
            return distribution;
        }

        List<Long> productIds = userProducts.stream()
                .map(up -> up.getProductId())
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return distribution;
        }

        distribution = productMapper.countByCategory(productIds);

        // 3. 写入缓存
        cacheService.setCategoryDistribution(userId, distribution);

        return distribution;
    }

    /**
     * 获取降价排行榜
     */
    public List<Map<String, Object>> getPriceDropRanking(Long userId, Integer limit) {
        // 1. 先尝试从缓存获取
        Object cached = cacheService.getPriceDropRanking(userId, limit);
        if (cached != null) {
            log.info("命中降价排行榜缓存，userId={}, limit={}", userId, limit);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ranking = (List<Map<String, Object>>) cached;
            return ranking;
        }

        log.info("未命中降价排行榜缓存，userId={}, limit={}", userId, limit);

        // 2. 缓存未命中，查询数据库
        List<Map<String, Object>> ranking= new ArrayList<>();

        var userProducts = userProductMapper.selectByUserId(userId);
        if (userProducts == null || userProducts.isEmpty()) {
            return ranking;
        }

        List<Long> productIds = userProducts.stream()
                .map(up -> up.getProductId())
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return ranking;
        }

        ranking = priceHistoryMapper.getPriceDropRanking(productIds, limit);

        // 3. 写入缓存
        cacheService.setPriceDropRanking(userId, limit, ranking);

        return ranking;
    }
}
