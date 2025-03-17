package com.shop.online.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO {
    
    /**
     * 购物车商品ID列表
     */
    private List<Integer> cartItemIds;
} 