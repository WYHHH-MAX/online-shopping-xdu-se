package com.shop.online.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.online.entity.Order;
import com.shop.online.dto.CreateOrderDTO;
import com.shop.online.dto.OrderQueryDTO;
import com.shop.online.vo.OrderVO;
import com.shop.online.common.PageResult;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 获取订单列表
     * @param queryDTO 查询条件
     * @return 分页订单列表
     */
    PageResult<OrderVO> getOrders(OrderQueryDTO queryDTO);

    /**
     * 创建订单
     * @param createOrderDTO 创建订单DTO
     * @return 订单创建结果
     */
    OrderVO createOrder(CreateOrderDTO createOrderDTO);

    /**
     * 支付订单
     * @param orderNo 订单号
     */
    void payOrder(String orderNo);

    /**
     * 取消订单
     * @param orderNo 订单号
     */
    void cancelOrder(String orderNo);

    /**
     * 确认收货
     * @param orderNo 订单号
     */
    void confirmOrder(String orderNo);

    /**
     * 获取订单详情
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderVO getOrderDetail(String orderNo);
} 