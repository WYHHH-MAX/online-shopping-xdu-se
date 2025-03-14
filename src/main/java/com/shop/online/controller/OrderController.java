package com.shop.online.controller;

import com.shop.online.dto.order.PayOrderDTO;
import com.shop.online.service.OrderService;
import com.shop.online.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/pay")
    public Result<Void> payOrder(@Validated @RequestBody PayOrderDTO dto) {
        orderService.payOrder(dto.getOrderNo());
        return Result.success();
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success();
    }
} 