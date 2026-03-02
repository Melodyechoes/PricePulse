package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.UserProduct;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.mapper.UserProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class UserProductService {

    @Autowired
    private UserProductMapper userProductMapper;

    /**
     * 关注商品
     */
    @Transactional
    public UserProduct followProduct(UserProduct userProduct) {
        // 参数验证
        validateUserProduct(userProduct);

        // 检查是否已关注
        UserProduct existing = userProductMapper.selectByUserIdAndProductId(
                userProduct.getUserId(), userProduct.getProductId());
        if (existing != null) {
            throw new BusinessException("已关注该商品");
        }

        // 设置默认值
        if (userProduct.getNotificationEnabled() == null) {
            userProduct.setNotificationEnabled(1);
        }
        if (userProduct.getPriceDropThreshold() == null) {
            userProduct.setPriceDropThreshold(new BigDecimal("5.00"));
        }

        // 插入关注记录
        int result = userProductMapper.insert(userProduct);
        if (result <= 0) {
            throw new BusinessException("关注商品失败");
        }

        log.info("用户 {} 成功关注商品 {}", userProduct.getUserId(), userProduct.getProductId());
        return userProduct;
    }

    /**
     * 取消关注
     */
    @Transactional
    public void unfollowProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            throw new BusinessException("用户ID和商品ID不能为空");
        }

        int result = userProductMapper.deleteByUserIdAndProductId(userId, productId);
        if (result <= 0) {
            throw new BusinessException("取消关注失败");
        }

        log.info("用户 {} 成功取消关注商品 {}", userId, productId);
    }

    /**
     * 获取用户关注列表
     */
    public List<UserProduct> getUserFollowedProducts(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        return userProductMapper.selectByUserId(userId);
    }

    /**
     * 获取关注某商品的用户列表
     */
    public List<UserProduct> getProductFollowers(Long productId) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        return userProductMapper.selectByProductId(productId);
    }

    /**
     * 更新关注设置
     */
    @Transactional
    public UserProduct updateFollowSettings(UserProduct userProduct) {
        if (userProduct.getId() == null) {
            throw new BusinessException("关注记录ID不能为空");
        }

        UserProduct existing = userProductMapper.selectById(userProduct.getId());
        if (existing == null) {
            throw new BusinessException("关注记录不存在");
        }

        // 更新设置
        int result = userProductMapper.update(userProduct);
        if (result <= 0) {
            throw new BusinessException("更新关注设置失败");
        }

        log.info("用户 {} 更新商品 {} 的关注设置",
                userProduct.getUserId(), userProduct.getProductId());
        return userProduct;
    }

    /**
     * 验证参数
     */
    private void validateUserProduct(UserProduct userProduct) {
        if (userProduct.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userProduct.getProductId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        if (userProduct.getTargetPrice() != null && userProduct.getTargetPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("期望价格必须大于0");
        }
        if (userProduct.getPriceDropThreshold() != null) {
            if (userProduct.getPriceDropThreshold().compareTo(BigDecimal.ZERO) < 0 ||
                    userProduct.getPriceDropThreshold().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("降价提醒阈值必须在0-100之间");
            }
        }
    }
}
