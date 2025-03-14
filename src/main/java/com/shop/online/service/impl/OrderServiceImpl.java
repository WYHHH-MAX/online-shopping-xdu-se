package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.entity.Order;
import com.shop.online.exception.BusinessException;
import com.shop.online.mapper.OrderMapper;
import com.shop.online.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo) {
        // 查询订单
        Order order = getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }
        
        // 更新订单状态为待发货
        order.setStatus(1);
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        // 查询订单
        Order order = getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }
        
        // 更新订单状态为已取消
        order.setStatus(4);
        updateById(order);
    }
} 