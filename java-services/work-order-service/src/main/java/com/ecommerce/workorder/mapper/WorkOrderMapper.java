package com.ecommerce.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.workorder.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工单Mapper接口
 */
@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    /**
     * 按状态统计工单数量
     */
    @Select("SELECT status, COUNT(*) as count FROM t_work_order WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 按优先级统计工单数量
     */
    @Select("SELECT priority, COUNT(*) as count FROM t_work_order WHERE deleted = 0 GROUP BY priority")
    List<Map<String, Object>> countByPriority();

    /**
     * 按分类统计工单数量
     */
    @Select("SELECT category_id, COUNT(*) as count FROM t_work_order WHERE deleted = 0 GROUP BY category_id")
    List<Map<String, Object>> countByCategory();

    /**
     * 统计客服工单数量
     */
    @Select("SELECT assignee_id, COUNT(*) as count FROM t_work_order WHERE deleted = 0 AND assignee_id IS NOT NULL GROUP BY assignee_id")
    List<Map<String, Object>> countByAssignee();

    /**
     * 查询超时工单
     */
    @Select("SELECT * FROM t_work_order WHERE deleted = 0 AND status IN ('PENDING', 'ASSIGNED', 'PROCESSING') " +
            "AND (sla_response_time < #{now} OR sla_resolve_time < #{now})")
    List<WorkOrder> findOverdueOrders(@Param("now") LocalDateTime now);

    /**
     * 查询即将超时工单(在指定时间内超时)
     */
    @Select("SELECT * FROM t_work_order WHERE deleted = 0 AND status IN ('PENDING', 'ASSIGNED', 'PROCESSING') " +
            "AND sla_response_time BETWEEN #{now} AND #{threshold}")
    List<WorkOrder> findNearDueOrders(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);

    /**
     * 统计某客服的工单数量
     */
    @Select("SELECT COUNT(*) FROM t_work_order WHERE deleted = 0 AND assignee_id = #{assigneeId} AND status IN ('ASSIGNED', 'PROCESSING')")
    int countActiveByAssignee(@Param("assigneeId") Long assigneeId);

    /**
     * 计算平均响应时长(分钟)
     */
    @Select("<script>" +
            "SELECT COALESCE(AVG(TIMESTAMPDIFF(MINUTE, created_at, first_response_time)), 0) " +
            "FROM t_work_order " +
            "WHERE deleted = 0 AND first_response_time IS NOT NULL " +
            "<if test='shopId != null'> AND shop_id = #{shopId} </if>" +
            "<if test='teamId != null'> AND team_id = #{teamId} </if>" +
            "<if test='assigneeId != null'> AND assignee_id = #{assigneeId} </if>" +
            "</script>")
    Double calculateAvgResponseTime(@Param("shopId") Long shopId, @Param("teamId") Long teamId, @Param("assigneeId") Long assigneeId);

    /**
     * 计算平均解决时长(小时)
     */
    @Select("<script>" +
            "SELECT COALESCE(AVG(TIMESTAMPDIFF(HOUR, created_at, resolved_at)), 0) " +
            "FROM t_work_order " +
            "WHERE deleted = 0 AND resolved_at IS NOT NULL " +
            "<if test='shopId != null'> AND shop_id = #{shopId} </if>" +
            "<if test='teamId != null'> AND team_id = #{teamId} </if>" +
            "<if test='assigneeId != null'> AND assignee_id = #{assigneeId} </if>" +
            "</script>")
    Double calculateAvgResolveTime(@Param("shopId") Long shopId, @Param("teamId") Long teamId, @Param("assigneeId") Long assigneeId);

    /**
     * 计算按时响应率(%)
     */
    @Select("<script>" +
            "SELECT COALESCE(COUNT(CASE WHEN first_response_time &lt; sla_response_time THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 0) " +
            "FROM t_work_order " +
            "WHERE deleted = 0 " +
            "<if test='shopId != null'> AND shop_id = #{shopId} </if>" +
            "<if test='teamId != null'> AND team_id = #{teamId} </if>" +
            "<if test='assigneeId != null'> AND assignee_id = #{assigneeId} </if>" +
            "</script>")
    Double calculateOnTimeResponseRate(@Param("shopId") Long shopId, @Param("teamId") Long teamId, @Param("assigneeId") Long assigneeId);

    /**
     * 计算按时解决率(%)
     */
    @Select("<script>" +
            "SELECT COALESCE(COUNT(CASE WHEN resolved_at &lt; sla_resolve_time THEN 1 END) * 100.0 / NULLIF(SUM(CASE WHEN resolved_at IS NOT NULL THEN 1 ELSE 0 END), 0), 0) " +
            "FROM t_work_order " +
            "WHERE deleted = 0 " +
            "<if test='shopId != null'> AND shop_id = #{shopId} </if>" +
            "<if test='teamId != null'> AND team_id = #{teamId} </if>" +
            "<if test='assigneeId != null'> AND assignee_id = #{assigneeId} </if>" +
            "</script>")
    Double calculateOnTimeResolveRate(@Param("shopId") Long shopId, @Param("teamId") Long teamId, @Param("assigneeId") Long assigneeId);

    /**
     * 计算平均满意度评分
     */
    @Select("<script>" +
            "SELECT COALESCE(AVG(satisfaction_score), 0) " +
            "FROM t_work_order " +
            "WHERE deleted = 0 AND satisfaction_score IS NOT NULL " +
            "<if test='shopId != null'> AND shop_id = #{shopId} </if>" +
            "<if test='teamId != null'> AND team_id = #{teamId} </if>" +
            "<if test='assigneeId != null'> AND assignee_id = #{assigneeId} </if>" +
            "</script>")
    Double calculateAvgSatisfactionScore(@Param("shopId") Long shopId, @Param("teamId") Long teamId, @Param("assigneeId") Long assigneeId);
}