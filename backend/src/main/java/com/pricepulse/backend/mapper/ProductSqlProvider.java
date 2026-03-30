package com.pricepulse.backend.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.util.List;

public class ProductSqlProvider {

    public String countByCategory(List<Long> productIds) {
        String ids = String.join(",", productIds.stream().map(String::valueOf).toArray(String[]::new));

        return new SQL() {{
            SELECT("category, COUNT(*) as count");
            FROM("products");
            WHERE("id IN (" + ids + ")");
            GROUP_BY("category");
        }}.toString();
    }

    public String countByPlatform(List<Long> productIds) {
        String ids = String.join(",", productIds.stream().map(String::valueOf).toArray(String[]::new));

        return new SQL() {{
            SELECT("platform as name, COUNT(*) as value");
            FROM("products");
            WHERE("id IN (" + ids + ")");
            GROUP_BY("platform");
        }}.toString();
    }

    public String searchWithFilters(String keyword, String category, String platform,
                                    BigDecimal minPrice, BigDecimal maxPrice) {
        return new SQL() {{
            SELECT("*");
            FROM("products");
            WHERE("status = 1");

            if (keyword != null && !keyword.isEmpty()) {
                WHERE("(name LIKE CONCAT('%', #{param1}, '%') OR brand LIKE CONCAT('%', #{param1}, '%'))");
            }

            if (category != null && !category.isEmpty()) {
                WHERE("category = #{param2}");
            }

            if (platform != null && !platform.isEmpty()) {
                WHERE("platform = #{param3}");
            }

            if (minPrice != null) {
                WHERE("current_price >= #{param4}");
            }

            if (maxPrice != null) {
                WHERE("current_price <= #{param5}");
            }

            ORDER_BY("created_at DESC");
        }}.toString();
    }
}
