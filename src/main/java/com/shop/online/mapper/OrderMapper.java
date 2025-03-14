package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
} 