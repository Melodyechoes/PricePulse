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
}
