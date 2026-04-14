package com.ecommerce.aftersale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.aftersale.entity.AfterSaleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 售后订单Mapper
 */
@Mapper
public interface AfterSaleOrderMapper extends BaseMapper<AfterSaleOrder> {

    /**
     * 统计各状态数量
     */
    @Select("SELECT status, COUNT(*) as count FROM t_after_sale_order WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计各类型数量
     */
    @Select("SELECT type, COUNT(*) as count FROM t_after_sale_order WHERE deleted = 0 GROUP BY type")
    List<Map<String, Object>> countByType();

    /**
     * 按日期统计售后数量
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM t_after_sale_order " +
            "WHERE deleted = 0 AND created_at BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> countByDate(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
}