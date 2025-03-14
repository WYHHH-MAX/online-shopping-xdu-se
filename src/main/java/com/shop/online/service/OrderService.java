package com.shop.online.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.online.entity.Order;

public interface OrderService extends IService<Order> {
    /**
     * 支付订单
     */
    void payOrder(String orderNo);
    
    /**
     * 取消订单
     */
    void cancelOrder(String orderNo);
} 