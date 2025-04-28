package com.shop.online.controller;

import com.shop.online.dto.CreateOrderDTO;
import com.shop.online.dto.OrderQueryDTO;
import com.shop.online.service.OrderService;
import com.shop.online.vo.OrderVO;
import com.shop.online.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * 订单控制器
 */

/**
 * @RestController = @Controller + @ResponseBody
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表
     */
    @GetMapping
    public Result getOrders(OrderQueryDTO queryDTO, HttpServletRequest request) {
//        log.info("========== 获取订单列表请求开始 ==========");
//        log.info("请求URL: {}", request.getRequestURI());
//        log.info("请求方法: {}", request.getMethod());
//        log.info("请求参数: {}", queryDTO);
        
        // 打印请求头
//        log.info("请求头信息:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
//            log.info("{}: {}", headerName, request.getHeader(headerName));
        }
        
        // 特别检查Authorization头
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            log.info("JWT Token存在: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
        } else {
            log.warn("Authorization头不存在或格式不正确");
        }
        
        try {
            Object result = orderService.getOrders(queryDTO);
//            log.info("订单列表获取成功");
//            log.info("========== 获取订单列表请求结束 ==========");
            return Result.success(result);
        } catch (Exception e) {
            log.error("订单列表获取失败", e);
            log.info("========== 获取订单列表请求失败 ==========");
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result createOrder(@RequestBody CreateOrderDTO createOrderDTO, HttpServletRequest request) {
//        log.info("========== 创建订单请求开始 ==========");
//        log.info("请求路径: {}", request.getRequestURI());
//        log.info("请求方法: {}", request.getMethod());
        
        // 打印请求头
        log.info("请求头信息:/");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
//            log.info("{}: {}", headerName, request.getHeader(headerName));
        }
        
        log.info("创建订单, 参数: {}", createOrderDTO);
        try {
            Object result = orderService.createOrder(createOrderDTO);
//            log.info("订单创建成功: {}", result);
//            log.info("========== 创建订单请求结束 ==========");
            return Result.success(result);
        } catch (Exception e) {
            log.error("订单创建失败", e);
            log.info("========== 创建订单请求失败 ==========");
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public Result payOrder(@RequestBody Map<String, String> params, HttpServletRequest request) {
//        log.info("支付订单, 参数: {}", params);
//        log.info("请求路径: {}", request.getRequestURI());
        
        String orderNo = params.get("orderNo");
        if (orderNo == null) {
            log.warn("订单号为空");
            return Result.error("订单号不能为空");
        }
        
        String paymentMethod = params.get("paymentMethod");
        Integer paymentMethodInt = null;
        
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            try {
                paymentMethodInt = Integer.parseInt(paymentMethod);
            } catch (NumberFormatException e) {
                log.warn("支付方式格式不正确: {}", paymentMethod);
                return Result.error("支付方式格式不正确");
            }
        }
        
        try {
            orderService.payOrder(orderNo, paymentMethodInt);
//            log.info("订单支付成功: {}", orderNo);
            return Result.success();
        } catch (Exception e) {
            log.error("订单支付失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderNo}/cancel")
    public Result cancelOrder(@PathVariable("orderNo") String orderNo) {
//        log.info("取消订单, 订单号: {}", orderNo);
        try {
            orderService.cancelOrder(orderNo);
//            log.info("订单取消成功: {}", orderNo);
            return Result.success();
        } catch (Exception e) {
            log.error("订单取消失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退款订单
     */
    @PostMapping("/{orderNo}/refund")
    public Result refundOrder(@PathVariable("orderNo") String orderNo) {
        log.info("申请退款, 订单号: {}", orderNo);
        try {
            orderService.refundOrder(orderNo);
            log.info("订单退款成功: {}", orderNo);
            return Result.success();
        } catch (Exception e) {
            log.error("订单退款失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/{orderNo}/confirm")
    public Result confirmOrder(@PathVariable("orderNo") String orderNo) {
//        log.info("确认收货, 订单号: {}", orderNo);
        try {
            orderService.confirmOrder(orderNo);
//            log.info("确认收货成功: {}", orderNo);
            return Result.success();
        } catch (Exception e) {
            log.error("确认收货失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderNo}")
    public Result getOrderDetail(@PathVariable("orderNo") String orderNo) {
//        log.info("获取订单详情, 订单号: {}", orderNo);
        try {
            OrderVO orderVO = orderService.getOrderDetail(orderNo);
//            log.info("获取订单详情成功: {}", orderNo);
            return Result.success(orderVO);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error(e.getMessage());
        }
    }
} 