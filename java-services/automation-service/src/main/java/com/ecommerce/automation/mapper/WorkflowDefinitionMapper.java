package com.ecommerce.automation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.automation.entity.WorkflowDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作流定义Mapper
 */
@Mapper
public interface WorkflowDefinitionMapper extends BaseMapper<WorkflowDefinition> {

    /**
     * 根据编码查询工作流定义
     */
    @Select("SELECT * FROM t_workflow_definition WHERE code = #{code} AND status = 'PUBLISHED' AND deleted = 0")
    WorkflowDefinition findByCode(@Param("code") String code);

    /**
     * 查询指定店铺的工作流定义
     */
    @Select("SELECT * FROM t_workflow_definition WHERE (shop_id = #{shopId} OR shop_id IS NULL) AND status = 'PUBLISHED' AND deleted = 0")
    List<WorkflowDefinition> findPublishedByShopId(@Param("shopId") Long shopId);

    /**
     * 查询最新版本的工作流定义
     */
    @Select("SELECT * FROM t_workflow_definition WHERE code = #{code} AND status = 'PUBLISHED' AND deleted = 0 ORDER BY version DESC LIMIT 1")
    WorkflowDefinition findLatestVersionByCode(@Param("code") String code);
}