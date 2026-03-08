package com.pricepulse.backend.controller;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats(@RequestParam Long userId) {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取价格趋势数据
     */
    @GetMapping("/price-trend")
    public Result<List<Map<String, Object>>> getPriceTrend(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            List<Map<String, Object>> trend = dashboardService.getPriceTrend(userId, days);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取价格趋势失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取分类分布数据
     */
    @GetMapping("/category-distribution")
    public Result<List<Map<String, Object>>> getCategoryDistribution(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> distribution = dashboardService.getCategoryDistribution(userId);
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取分类分布失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取降价排行榜
     */
    @GetMapping("/price-drop-ranking")
    public Result<List<Map<String, Object>>> getPriceDropRanking(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> ranking = dashboardService.getPriceDropRanking(userId, limit);
            return Result.success(ranking);
        } catch (Exception e) {
            log.error("获取降价排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }
}
