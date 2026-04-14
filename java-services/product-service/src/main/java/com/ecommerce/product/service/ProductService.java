package com.ecommerce.product.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    private final SourceMatchService sourceMatchService;

    /**
     * 分页查询商品列表
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param name        商品名称(模糊查询)
     * @param categoryId  分类ID
     * @param status      状态
     * @return 分页结果
     */
    public IPage<Product> getProductList(Integer pageNum, Integer pageSize, String name, Long categoryId, Integer status) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(name)) {
            wrapper.like(Product::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getWeight, Product::getCreateTime);

        return this.page(page, wrapper);
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品信息
     */
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在: " + id);
        }
        return product;
    }

    /**
     * 创建商品
     *
     * @param product 商品信息
     * @return 创建后的商品
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "product", allEntries = true)
    public Product createProduct(Product product) {
        // 生成商品编码
        if (StrUtil.isBlank(product.getProductCode())) {
            product.setProductCode("PRD" + IdUtil.getSnowflakeNextIdStr());
        }

        // 校验商品编码唯一性
        Product existProduct = baseMapper.selectByProductCode(product.getProductCode());
        if (existProduct != null) {
            throw new RuntimeException("商品编码已存在: " + product.getProductCode());
        }

        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(0); // 默认下架
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getSoldCount() == null) {
            product.setSoldCount(0);
        }
        if (product.getViewCount() == null) {
            product.setViewCount(0);
        }
        if (product.getWeight() == null) {
            product.setWeight(0);
        }
        if (product.getIsHot() == null) {
            product.setIsHot(false);
        }
        if (product.getIsNew() == null) {
            product.setIsNew(false);
        }
        if (product.getIsRecommend() == null) {
            product.setIsRecommend(false);
        }
        if (product.getSource() == null) {
            product.setSource("manual");
        }

        this.save(product);
        log.info("创建商品成功: id={}, name={}, code={}", product.getId(), product.getName(), product.getProductCode());
        return product;
    }

    /**
     * 更新商品
     *
     * @param id      商品ID
     * @param product 商品信息
     * @return 更新后的商品
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "product", key = "#id")
    public Product updateProduct(Long id, Product product) {
        Product existProduct = this.getById(id);
        if (existProduct == null) {
            throw new RuntimeException("商品不存在: " + id);
        }

        // 更新字段
        product.setId(id);
        // 不允许修改的字段
        product.setProductCode(null);
        product.setCreateTime(null);
        product.setCreateBy(null);

        this.updateById(product);
        log.info("更新商品成功: id={}", id);
        return this.getById(id);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "product", key = "#id")
    public void deleteProduct(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在: " + id);
        }

        this.removeById(id);
        log.info("删除商品成功: id={}, name={}", id, product.getName());
    }

    /**
     * 上架商品
     *
     * @param id 商品ID
     */
    @CacheEvict(value = "product", key = "#id")
    public void publishProduct(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在: " + id);
        }

        product.setStatus(1);
        this.updateById(product);
        log.info("商品上架成功: id={}", id);
    }

    /**
     * 下架商品
     *
     * @param id 商品ID
     */
    @CacheEvict(value = "product", key = "#id")
    public void unpublishProduct(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在: " + id);
        }

        product.setStatus(0);
        this.updateById(product);
        log.info("商品下架成功: id={}", id);
    }

    /**
     * 更新库存
     *
     * @param id       商品ID
     * @param quantity 数量变化(正数增加，负数减少)
     */
    @CacheEvict(value = "product", key = "#id")
    public void updateStock(Long id, Integer quantity) {
        int rows = baseMapper.updateStock(id, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存更新失败: " + id);
        }
        log.info("库存更新成功: id={}, quantity={}", id, quantity);
    }

    /**
     * 根据分类查询商品
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    public List<Product> getByCategoryId(Long categoryId) {
        return baseMapper.selectByCategoryId(categoryId);
    }

    /**
     * 获取热门商品
     *
     * @param limit 数量限制
     * @return 商品列表
     */
    public List<Product> getHotProducts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return baseMapper.selectHotProducts(limit);
    }

    /**
     * 获取新品商品
     *
     * @param limit 数量限制
     * @return 商品列表
     */
    public List<Product> getNewProducts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return baseMapper.selectNewProducts(limit);
    }

    /**
     * 增加浏览次数
     *
     * @param id 商品ID
     */
    public void incrementViewCount(Long id) {
        Product product = this.getById(id);
        if (product != null) {
            product.setViewCount(product.getViewCount() + 1);
            this.updateById(product);
        }
    }
}