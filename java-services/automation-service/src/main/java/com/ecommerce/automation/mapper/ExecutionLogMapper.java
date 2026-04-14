package com.ecommerce.automation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.automation.entity.ExecutionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行日志Mapper
 */
@Mapper
public interface ExecutionLogMapper extends BaseMapper<ExecutionLog> {

    /**
     * 查询规则的执行日志
     */
    @Select("SELECT * FROM t_execution_log WHERE log_type = 'RULE' AND related_id = #{ruleId} ORDER BY created_at DESC LIMIT #{limit}")
    List<ExecutionLog> findByRuleId(@Param("ruleId") Long ruleId, @Param("limit") int limit);

    /**
     * 查询工作流实例的执行日志
     */
    @Select("SELECT * FROM t_execution_log WHERE log_type = 'WORKFLOW' AND related_id = #{instanceId} ORDER BY created_at DESC")
    List<ExecutionLog> findByWorkflowInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 查询会话的执行日志
     */
    @Select("SELECT * FROM t_execution_log WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    List<ExecutionLog> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询指定时间段的执行日志
     */
    @Select("SELECT * FROM t_execution_log WHERE executed_at BETWEEN #{startTime} AND #{endTime} ORDER BY executed_at DESC")
    List<ExecutionLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计规则执行成功率
     */
    @Select("SELECT COUNT(CASE WHEN status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(*) FROM t_execution_log WHERE log_type = 'RULE' AND related_id = #{ruleId}")
    Double calculateSuccessRate(@Param("ruleId") Long ruleId);
}