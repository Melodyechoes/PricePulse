package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.PriceHistory;
import com.pricepulse.backend.mapper.PriceHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PriceHistoryService {

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    /**
     * 获取商品的价格历史记录
     */
    public List<PriceHistory> getPriceHistory(Long productId) {
        log.info("查询价格历史，productId: {}", productId);
        List<PriceHistory> history = priceHistoryMapper.selectByProductId(productId);
        log.info("找到 {} 条价格记录", history.size());
        return history;
    }

    /**
     * 添加价格历史记录
     */
    public void addPriceHistory(PriceHistory priceHistory) {
        priceHistoryMapper.insert(priceHistory);
    }
}
