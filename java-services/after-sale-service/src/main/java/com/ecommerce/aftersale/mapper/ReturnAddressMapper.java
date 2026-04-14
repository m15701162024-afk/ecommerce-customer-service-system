package com.ecommerce.aftersale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.aftersale.entity.ReturnAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退货地址Mapper
 */
@Mapper
public interface ReturnAddressMapper extends BaseMapper<ReturnAddress> {
}