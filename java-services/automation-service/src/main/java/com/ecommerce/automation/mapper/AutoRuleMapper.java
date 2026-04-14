package com.ecommerce.automation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.automation.entity.AutoRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 自动化规则Mapper
 */
@Mapper
public interface AutoRuleMapper extends BaseMapper<AutoRule> {

    /**
     * 查询启用的规则(按优先级排序)
     */
    @Select("SELECT * FROM t_auto_rule WHERE status = 'ENABLED' AND deleted = 0 ORDER BY priority ASC")
    List<AutoRule> findEnabledRulesOrderByPriority();

    /**
     * 查询指定店铺的启用的规则
     */
    @Select("SELECT * FROM t_auto_rule WHERE (shop_id = #{shopId} OR shop_id IS NULL) AND status = 'ENABLED' AND deleted = 0 ORDER BY priority ASC")
    List<AutoRule> findEnabledRulesByShopId(@Param("shopId") Long shopId);

    /**
     * 查询指定类型的启用规则
     */
    @Select("SELECT * FROM t_auto_rule WHERE rule_type = #{ruleType} AND status = 'ENABLED' AND deleted = 0 ORDER BY priority ASC")
    List<AutoRule> findEnabledRulesByType(@Param("ruleType") String ruleType);

    /**
     * 增加执行次数
     */
    @Update("UPDATE t_auto_rule SET execution_count = execution_count + 1, last_executed_at = NOW() WHERE id = #{id}")
    int incrementExecutionCount(@Param("id") Long id);
}