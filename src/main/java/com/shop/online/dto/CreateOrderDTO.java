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
    
    /**
     * 是否直接购买
     */
    private Boolean directBuy;
    
    /**
     * 直接购买的商品ID
     */
    private Integer productId;
    
    /**
     * 直接购买的商品数量
     */
    private Integer quantity;
    
    /**
     * 收货人手机号
     */
    private String phone;
    
    /**
     * 收货地址
     */
    private String location;
} 