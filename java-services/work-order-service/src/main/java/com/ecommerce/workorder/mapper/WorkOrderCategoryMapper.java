package com.ecommerce.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.workorder.entity.WorkOrderCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单分类Mapper接口
 */
@Mapper
public interface WorkOrderCategoryMapper extends BaseMapper<WorkOrderCategory> {

}