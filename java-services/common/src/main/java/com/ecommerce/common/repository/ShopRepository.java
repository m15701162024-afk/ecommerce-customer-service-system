package com.ecommerce.common.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.common.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 店铺Repository
 */
@Mapper
public interface ShopRepository extends BaseMapper<Shop> {

    /**
     * 根据平台和状态查询店铺ID列表
     */
    @Select("SELECT id FROM shop WHERE platform = #{platform} AND status = #{status} AND deleted = 0")
    List<Long> findIdsByPlatformAndStatus(@Param("platform") String platform, @Param("status") Integer status);

    /**
     * 根据平台和状态查询店铺列表
     */
    @Select("SELECT * FROM shop WHERE platform = #{platform} AND status = #{status} AND deleted = 0")
    List<Shop> findByPlatformAndStatus(@Param("platform") String platform, @Param("status") Integer status);

    /**
     * 根据平台店铺ID查询店铺
     */
    @Select("SELECT * FROM shop WHERE platform = #{platform} AND platform_shop_id = #{platformShopId} AND deleted = 0")
    Shop findByPlatformAndPlatformShopId(@Param("platform") String platform, @Param("platformShopId") String platformShopId);
}