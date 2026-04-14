package com.ecommerce.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Product> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据商品编码查询
     *
     * @param productCode 商品编码
     * @return 商品信息
     */
    Product selectByProductCode(@Param("productCode") String productCode);

    /**
     * 批量更新库存
     *
     * @param productId 商品ID
     * @param quantity  数量(正数增加，负数减少)
     * @return 影响行数
     */
    int updateStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 查询热门商品
     *
     * @param limit 数量限制
     * @return 商品列表
     */
    List<Product> selectHotProducts(@Param("limit") Integer limit);

    /**
     * 查询新品商品
     *
     * @param limit 数量限制
     * @return 商品列表
     */
    List<Product> selectNewProducts(@Param("limit") Integer limit);
}