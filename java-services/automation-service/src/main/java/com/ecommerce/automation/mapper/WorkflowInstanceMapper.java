package com.ecommerce.automation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.automation.entity.WorkflowInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 工作流实例Mapper
 */
@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {

    /**
     * 查询运行中的工作流实例
     */
    @Select("SELECT * FROM t_workflow_instance WHERE status = 'RUNNING' AND deleted = 0")
    List<WorkflowInstance> findRunningInstances();

    /**
     * 查询会话关联的工作流实例
     */
    @Select("SELECT * FROM t_workflow_instance WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY created_at DESC")
    List<WorkflowInstance> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询业务关联的工作流实例
     */
    @Select("SELECT * FROM t_workflow_instance WHERE business_id = #{businessId} AND business_type = #{businessType} AND deleted = 0 ORDER BY created_at DESC")
    List<WorkflowInstance> findByBusinessId(@Param("businessId") String businessId, @Param("businessType") String businessType);

    /**
     * 更新工作流状态
     */
    @Update("UPDATE t_workflow_instance SET status = #{status}, current_step_id = #{currentStepId}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("currentStepId") String currentStepId);

    /**
     * 标记工作流完成
     */
    @Update("UPDATE t_workflow_instance SET status = 'COMPLETED', finished_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    int markCompleted(@Param("id") Long id);

    /**
     * 标记工作流失败
     */
    @Update("UPDATE t_workflow_instance SET status = 'FAILED', error_message = #{errorMessage}, finished_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}