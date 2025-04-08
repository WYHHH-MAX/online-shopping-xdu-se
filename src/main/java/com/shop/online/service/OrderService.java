package com.shop.online.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.online.entity.Order;
import com.shop.online.dto.CreateOrderDTO;
import com.shop.online.dto.OrderQueryDTO;
import com.shop.online.vo.OrderVO;
import com.shop.online.common.result.PageResult;
import java.util.Map;
import java.io.IOException;

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

    /**
     * 获取卖家订单列表
     * @param params 查询参数
     * @return 订单列表
     */
    PageResult<OrderVO> getSellerOrders(Map<String, Object> params);

    /**
     * 卖家发货
     * @param orderNo 订单号
     * @param sellerId 卖家ID
     * @return 是否成功
     */
    boolean shipOrder(String orderNo, Long sellerId);

    /**
     * 统计卖家指定状态的订单数量
     * @param sellerId 卖家ID
     * @param status 订单状态
     * @return 订单数量
     */
    int countSellerOrdersByStatus(Long sellerId, Integer status);

    /**
     * 统计卖家订单总数
     * @param sellerId 卖家ID
     * @return 订单总数
     */
    int countSellerOrders(Long sellerId);

    /**
     * 获取销售数据分析
     * @param params 查询参数，包含sellerId、startDate、endDate、period等
     * @return 销售数据分析结果
     */
    Map<String, Object> getSalesAnalytics(Map<String, Object> params);

    /**
     * 导出财务报表
     * @param params 查询参数，包含sellerId、startDate、endDate、reportType等
     * @param outputStream 输出流
     * @param fileType 文件类型，csv或excel
     * @throws IOException IO异常
     */
    void exportFinancialReport(Map<String, Object> params, java.io.OutputStream outputStream, String fileType) throws java.io.IOException;
} 