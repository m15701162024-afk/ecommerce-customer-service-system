package com.ecommerce.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_work_order")
public class WorkOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 工单描述
     */
    private String description;

    /**
     * 工单分类ID
     */
    private Long categoryId;

    /**
     * 工单状态: PENDING-待处理, ASSIGNED-已分配, PROCESSING-处理中, RESOLVED-已解决, CLOSED-已关闭, REOPENED-已重开
     */
    private String status;

    /**
     * 优先级: URGENT-紧急, HIGH-高, NORMAL-普通, LOW-低
     */
    private String priority;

    /**
     * 关联会话ID
     */
    private Long sessionId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 买家名称
     */
    private String buyerName;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNoRef;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 平台来源
     */
    private String platform;

    /**
     * 当前处理人ID
     */
    private Long assigneeId;

    /**
     * 当前处理人名称
     */
    private String assigneeName;

    /**
     * 所属团队ID
     */
    private Long teamId;

    /**
     * SLA响应截止时间
     */
    private LocalDateTime slaResponseTime;

    /**
     * SLA解决截止时间
     */
    private LocalDateTime slaResolveTime;

    /**
     * 首次响应时间
     */
    private LocalDateTime firstResponseTime;

    /**
     * 解决时间
     */
    private LocalDateTime resolvedAt;

    /**
     * 关闭时间
     */
    private LocalDateTime closedAt;

    /**
     * 解决方案
     */
    private String solution;

    /**
     * 满意度评分(1-5)
     */
    private Integer satisfactionScore;

    /**
     * 评价内容
     */
    private String feedback;

    /**
     * 标签(JSON数组)
     */
    private String tags;

    /**
     * 附件(JSON数组)
     */
    private String attachments;

    /**
     * 来源渠道
     */
    private String source;

    /**
     * 扩展字段(JSON)
     */
    private String extra;
}