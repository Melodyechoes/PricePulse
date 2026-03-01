package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.Product;
import com.pricepulse.backend.common.exception.BusinessException;
import com.pricepulse.backend.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加商品
     */
    @Transactional
    public Product addProduct(Product product) {
        // 参数验证
        validateProduct(product);

        // 检查是否已存在相同商品
        if (StringUtils.hasText(product.getPlatformId())) {
            Product existingProduct = productMapper.selectByPlatformId(
                    product.getPlatformId(), product.getPlatform().name());
            if (existingProduct != null) {
                throw new BusinessException("该商品已存在");
            }
        }

        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getStockStatus() == null) {
            product.setStockStatus(1);
        }

        // 插入商品
        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException("商品添加失败");
        }

        log.info("商品添加成功，ID: {}", product.getId());
        return product;
    }

    /**
     * 根据ID查询商品
     */
    public Product getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("商品ID不能为空");
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        return product;
    }

    /**
     * 获取所有商品
     */
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    /**
     * 更新商品信息
     */
    @Transactional
    public Product updateProduct(Product product) {
        // 验证ID
        if (product.getId() == null || product.getId() <= 0) {
            throw new BusinessException("商品ID不能为空");
        }

        // 验证商品是否存在
        Product existingProduct = productMapper.selectById(product.getId());
        if (existingProduct == null) {
            throw new BusinessException("商品不存在");
        }

        // 参数验证
        validateProduct(product);

        // 更新商品
        int result = productMapper.update(product);
        if (result <= 0) {
            throw new BusinessException("商品更新失败");
        }

        log.info("商品更新成功，ID: {}", product.getId());
        return product;
    }

    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("商品ID不能为空");
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        int result = productMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException("商品删除失败");
        }

        log.info("商品删除成功，ID: {}", id);
    }

    /**
     * 根据分类查询商品
     */
    public List<Product> getProductsByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            throw new BusinessException("分类不能为空");
        }
        return productMapper.selectByCategory(category);
    }

    /**
     * 根据品牌查询商品
     */
    public List<Product> getProductsByBrand(String brand) {
        if (!StringUtils.hasText(brand)) {
            throw new BusinessException("品牌不能为空");
        }
        return productMapper.selectByBrand(brand);
    }

    /**
     * 根据价格范围查询商品
     */
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new BusinessException("价格范围不能为空");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("价格不能为负数");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("最小价格不能大于最大价格");
        }
        return productMapper.selectByPriceRange(minPrice, maxPrice);
    }

    /**
     * 验证商品参数
     */
    private void validateProduct(Product product) {
        if (!StringUtils.hasText(product.getName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (!StringUtils.hasText(product.getUrl())) {
            throw new BusinessException("商品链接不能为空");
        }
        if (product.getPlatform() == null) {
            throw new BusinessException("商品平台不能为空");
        }
        if (product.getCurrentPrice() == null) {
            throw new BusinessException("商品价格不能为空");
        }
        if (product.getCurrentPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格必须大于0");
        }
    }
}
