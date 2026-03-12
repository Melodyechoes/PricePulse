package com.pricepulse.backend.mapper;

import org.apache.ibatis.jdbc.SQL;

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
}
