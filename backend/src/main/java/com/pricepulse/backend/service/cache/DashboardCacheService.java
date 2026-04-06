package com.pricepulse.backend.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DashboardCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DASHBOARD_STATS_KEY = "dashboard:stats:";
    private static final String DASHBOARD_TREND_KEY = "dashboard:trend:";
    private static final String DASHBOARD_CATEGORY_KEY = "dashboard:category:";
    private static final String DASHBOARD_NOTIFICATION_KEY = "dashboard:notification:";
    private static final String DASHBOARD_PLATFORM_KEY = "dashboard:platform:";

    // 缓存时间：5 分钟
    private static final long CACHE_TTL_MINUTES = 5;

    /**
     * 获取统计数据缓存
     */
    public Object getStats(Long userId) {
        String key = DASHBOARD_STATS_KEY + userId;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取统计数据缓存失败，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 设置统计数据缓存
     */
    public void setStats(Long userId, Object data) {
        String key = DASHBOARD_STATS_KEY + userId;
        try {
            redisTemplate.opsForValue().set(key, data, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("设置统计数据缓存，key={}, TTL={}分钟", key, CACHE_TTL_MINUTES);
        } catch (Exception e) {
            log.error("设置统计数据缓存失败，userId={}", userId, e);
        }
    }

    /**
     * 获取价格趋势缓存
     */
    public Object getPriceTrend(Long userId, Integer days) {
        String key = DASHBOARD_TREND_KEY + userId + ":" + days;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取价格趋势缓存失败，userId={}, days={}", userId, days, e);
            return null;
        }
    }

    /**
     * 设置价格趋势缓存
     */
    public void setPriceTrend(Long userId, Integer days, Object data) {
        String key = DASHBOARD_TREND_KEY + userId + ":" + days;
        try {
            redisTemplate.opsForValue().set(key, data, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("设置价格趋势缓存，key={}, TTL={}分钟", key, CACHE_TTL_MINUTES);
        } catch (Exception e) {
            log.error("设置价格趋势缓存失败，userId={}, days={}", userId, days, e);
        }
    }

    /**
     * 获取分类分布缓存
     */
    public Object getCategoryDistribution(Long userId) {
        String key = DASHBOARD_CATEGORY_KEY + userId;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取分类分布缓存失败，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 设置分类分布缓存
     */
    public void setCategoryDistribution(Long userId, Object data) {
        String key = DASHBOARD_CATEGORY_KEY + userId;
        try {
            redisTemplate.opsForValue().set(key, data, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("设置分类分布缓存，key={}, TTL={}分钟", key, CACHE_TTL_MINUTES);
        } catch (Exception e) {
            log.error("设置分类分布缓存失败，userId={}", userId, e);
        }
    }

    /**
     * 获取通知统计缓存
     */
    public Object getNotificationStats(Long userId) {
        String key = DASHBOARD_NOTIFICATION_KEY + userId;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取通知统计缓存失败，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 设置通知统计缓存
     */
    public void setNotificationStats(Long userId, Object data) {
        String key = DASHBOARD_NOTIFICATION_KEY + userId;
        try {
            redisTemplate.opsForValue().set(key, data, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("设置通知统计缓存，key={}, TTL={}分钟", key, CACHE_TTL_MINUTES);
        } catch (Exception e) {
            log.error("设置通知统计缓存失败，userId={}", userId, e);
        }
    }

    /**
     * 获取平台分布缓存
     */
    public Object getPlatformDistribution(Long userId) {
        String key = DASHBOARD_PLATFORM_KEY + userId;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取平台分布缓存失败，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 设置平台分布缓存
     */
    public void setPlatformDistribution(Long userId, Object data) {
        String key = DASHBOARD_PLATFORM_KEY + userId;
        try {
            redisTemplate.opsForValue().set(key, data, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("设置平台分布缓存，key={}, TTL={}分钟", key, CACHE_TTL_MINUTES);
        } catch (Exception e) {
            log.error("设置平台分布缓存失败，userId={}", userId, e);
        }
    }

    /**
     * 删除用户的所有 Dashboard 缓存（当数据更新时调用）
     */
    public void deleteUserDashboardCache(Long userId) {
        try {
            String pattern = "dashboard:*:" + userId + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("删除用户 {} 的 Dashboard 缓存，共 {} 个键", userId, keys.size());
            }
        } catch (Exception e) {
            log.error("删除 Dashboard 缓存失败，userId={}", userId, e);
        }
    }
}
