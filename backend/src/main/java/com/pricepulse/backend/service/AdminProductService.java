package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员商品审核服务
 */
@Service
@Slf4j
public class AdminProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取所有商品列表（分页，支持审核状态过滤）
     */
    public Map<String, Object> getAllProducts(Integer page, Integer pageSize, Integer status) {
        log.info("获取商品列表，page={}, pageSize={}, status={}", page, pageSize, status);
        
        List<Product> products;
        if (status != null) {
            products = productMapper.selectByStatus(status);
        } else {
            products = productMapper.selectAllProducts();
        }
        
        // 简单分页
        int total = products.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        if (fromIndex >= total) {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return result;
        }
        
        List<Product> pageData = products.subList(fromIndex, toIndex);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageData);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return result;
    }

    /**
     * 根据 ID 获取商品详情
     */
    public Product getProductById(Long id) {
        log.info("获取商品详情，id={}", id);
        return productMapper.selectById(id);
    }

    /**
     * 更新商品审核状态
     */
    @Transactional
    public void updateProductStatus(Long productId, Integer status, String auditComment) {
        log.info("更新商品审核状态，productId={}, status={}, comment={}", productId, status, auditComment);
        
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        productMapper.updateStatus(productId, status);
        
        // 如果有审核意见，更新到描述中
        if (auditComment != null && !auditComment.trim().isEmpty()) {
            String newDescription = product.getDescription() != null ? 
                product.getDescription() + "\n[审核意见] " + auditComment : 
                "[审核意见] " + auditComment;
            productMapper.updateDescription(productId, newDescription);
        }
        
        log.info("商品 {} 审核状态已更新为 {}", productId, status);
    }

    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("删除商品，id={}", productId);
        
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        productMapper.deleteById(productId);
        log.info("商品 {} 已删除", productId);
    }

    /**
     * 统计商品数量
     */
    public Map<String, Object> getProductStatistics() {
        log.info("获取商品统计数据");
        
        List<Product> allProducts = productMapper.selectAllProducts();
        
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream().filter(p -> p.getStatus() == 1).count();
        long inactiveProducts = totalProducts - activeProducts;
        
        // 按平台统计
        Map<String, Long> platformStats = new HashMap<>();
        allProducts.forEach(p -> {
            String platform = p.getPlatform() != null ? p.getPlatform() : "OTHER";
            platformStats.put(platform, platformStats.getOrDefault(platform, 0L) + 1);
        });
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("activeProducts", activeProducts);
        stats.put("inactiveProducts", inactiveProducts);
        stats.put("platformStats", platformStats);
        
        return stats;
    }

    /**
     * 批量删除商品
     */
    @Transactional
    public void batchDeleteProducts(List<Long> productIds) {
        log.info("批量删除商品，ids={}", productIds);
        
        for (Long id : productIds) {
            try {
                productMapper.deleteById(id);
                log.info("商品 {} 已删除", id);
            } catch (Exception e) {
                log.error("删除商品 {} 失败", id, e);
            }
        }
    }
}
