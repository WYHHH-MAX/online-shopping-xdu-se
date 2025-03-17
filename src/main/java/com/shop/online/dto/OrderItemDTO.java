package com.shop.online.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项DTO，用于传输订单项数据
 */
@Data
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
    
    // 用于从Cart对象创建OrderItemDTO
    public static OrderItemDTO fromCart(com.shop.online.entity.Cart cart, 
                                        com.shop.online.entity.Product product) {
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setPrice(product.getPrice());
        item.setQuantity(cart.getQuantity());
        item.setTotalAmount(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        item.setCreatedTime(LocalDateTime.now());
        item.setUpdatedTime(LocalDateTime.now());
        item.setDeleted(0);
        return item;
    }
} 