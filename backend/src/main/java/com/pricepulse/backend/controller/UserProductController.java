package com.pricepulse.backend.controller;

import com.pricepulse.backend.common.response.Result;
import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.service.UserProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-products")
@Slf4j
public class UserProductController {

    @Autowired
    private UserProductService userProductService;

    /**
     * 关注商品
     */
    @PostMapping
    public Result<UserProduct> followProduct(@RequestBody UserProduct userProduct) {
        try {
            UserProduct result = userProductService.followProduct(userProduct);
            return Result.success("关注成功", result);
        } catch (Exception e) {
            log.error("关注商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消关注
     */
    @DeleteMapping
    public Result<Void> unfollowProduct(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            userProductService.unfollowProduct(userId, productId);
            return Result.success(); // 修复：使用无参的成功方法
        } catch (Exception e) {
            log.error("取消关注失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户关注列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<UserProduct>> getUserFollowedProducts(@PathVariable Long userId) {
        try {
            List<UserProduct> products = userProductService.getUserFollowedProducts(userId);
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取关注列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取关注某商品的用户
     */
    @GetMapping("/product/{productId}")
    public Result<List<UserProduct>> getProductFollowers(@PathVariable Long productId) {
        try {
            List<UserProduct> users = userProductService.getProductFollowers(productId);
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取商品关注者失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新关注设置
     */
    @PutMapping("/{id}")
    public Result<UserProduct> updateFollowSettings(@PathVariable Long id,
                                                    @RequestBody UserProduct userProduct) {
        try {
            userProduct.setId(id);
            UserProduct result = userProductService.updateFollowSettings(userProduct);
            return Result.success("设置更新成功", result);
        } catch (Exception e) {
            log.error("更新关注设置失败", e);
            return Result.error(e.getMessage());
        }
    }
}
