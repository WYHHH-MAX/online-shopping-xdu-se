package com.shop.online.dto.cart;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Boolean selected;
} 