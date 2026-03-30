package com.pricepulse.backend.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.time.LocalDateTime;
import java.util.List;

public class PriceHistorySqlProvider {


    public String getAveragePriceByDays(List<Long> productIds, LocalDateTime startDate) {
        String ids = String.join(",", productIds.stream().map(String::valueOf).toArray(String[]::new));

        return new SQL() {{
            SELECT("DATE(checked_at) as date, AVG(price) as avgPrice");
            FROM("price_history");
            WHERE("product_id IN (" + ids + ")");
            WHERE("checked_at >= '" + startDate + "'");
            GROUP_BY("DATE(checked_at)");
            ORDER_BY("date ASC");
        }}.toString();
    }

    public String getPriceDropRanking(List<Long> productIds, Integer limit) {
        String ids = String.join(",", productIds.stream().map(String::valueOf).toArray(String[]::new));

        return new SQL() {{
            SELECT("p.id as productId, p.name as productName");
            SELECT("p.original_price as originalPrice, p.current_price as currentPrice");
            SELECT("ROUND(((p.original_price - p.current_price) / p.original_price) * 100, 2) as dropPercent");
            FROM("products p");
            WHERE("p.id IN (" + ids + ")");
            WHERE("p.current_price < p.original_price");
            ORDER_BY("dropPercent DESC");
            LIMIT(String.valueOf(limit));
        }}.toString();
    }

    public String getVolatilityRanking(List<Long> productIds, Integer limit) {
        String ids = String.join(",", productIds.stream().map(String::valueOf).toArray(String[]::new));

        return new SQL() {{
            SELECT("p.id as productId, p.name as productName");
            SELECT("MAX(ph.price) as maxPrice, MIN(ph.price) as minPrice");
            SELECT("AVG(ph.price) as avgPrice");
            SELECT("ROUND((MAX(ph.price) - MIN(ph.price)) / AVG(ph.price) * 100, 2) as volatility");
            FROM("price_history ph");
            INNER_JOIN("products p ON ph.product_id = p.id");
            WHERE("ph.product_id IN (" + ids + ")");
            GROUP_BY("p.id, p.name");
            ORDER_BY("volatility DESC");
            LIMIT(String.valueOf(limit));
        }}.toString();
    }

    public String getDailyPricesByProducts(List<Long> productIds, LocalDateTime startDate) {
        String ids = String.join(",", productIds.stream().map(String::valueOf).toArray(String[]::new));

        return new SQL() {{
            SELECT("ph.product_id, p.name as product_name, DATE(ph.checked_at) as date, ph.price");
            FROM("price_history ph");
            FROM("products p");
            WHERE("ph.product_id = p.id");
            WHERE("ph.product_id IN (" + ids + ")");
            WHERE("ph.checked_at >= '" + startDate + "'");
            ORDER_BY("ph.product_id, DATE(ph.checked_at) ASC");
        }}.toString();
    }

}
