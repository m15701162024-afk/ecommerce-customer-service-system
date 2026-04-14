package com.ecommerce.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.workorder.entity.WorkOrderFlow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单流转记录Mapper接口
 */
@Mapper
public interface WorkOrderFlowMapper extends BaseMapper<WorkOrderFlow> {

}