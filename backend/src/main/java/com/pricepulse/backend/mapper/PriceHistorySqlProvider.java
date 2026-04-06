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
