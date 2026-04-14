package com.ecommerce.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单流转记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_work_order_flow")
public class WorkOrderFlow extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工单ID
     */
    private Long workOrderId;

    /**
     * 操作类型: CREATE-创建, ASSIGN-分配, TRANSFER-转派, PROCESS-处理, RESOLVE-解决, CLOSE-关闭, REOPEN-重开
     */
    private String action;

    /**
     * 操作前状态
     */
    private String fromStatus;

    /**
     * 操作后状态
     */
    private String toStatus;

    /**
     * 操作前处理人ID
     */
    private Long fromAssigneeId;

    /**
     * 操作前处理人名称
     */
    private String fromAssigneeName;

    /**
     * 操作后处理人ID
     */
    private Long toAssigneeId;

    /**
     * 操作后处理人名称
     */
    private String toAssigneeName;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作备注
     */
    private String remark;

    /**
     * 操作来源
     */
    private String source;
}