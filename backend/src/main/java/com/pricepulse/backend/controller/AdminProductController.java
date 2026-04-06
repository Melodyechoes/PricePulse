package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.service.AdminProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员商品审核接口
 */
@RestController
@RequestMapping("/api/admin/products")
@Slf4j
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    /**
     * 获取商品列表（支持状态过滤）
     */
    @GetMapping
    public Result<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = adminProductService.getAllProducts(page, pageSize, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        try {
            Product product = adminProductService.getProductById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            return Result.success(product);
        } catch (Exception e) {
            log.error("获取商品详情失败，id={}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品审核状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditComment) {
        try {
            adminProductService.updateProductStatus(id, status, auditComment);
            return Result.success();
        } catch (Exception e) {
            log.error("更新商品审核状态失败，id={}, status={}", id, status, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        try {
            adminProductService.deleteProduct(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除商品失败，id={}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除商品
     */
    @PostMapping("/batch-delete")
    public Result<Void> batchDeleteProducts(@RequestBody List<Long> productIds) {
        try {
            adminProductService.batchDeleteProducts(productIds);
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = adminProductService.getProductStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取商品统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}
