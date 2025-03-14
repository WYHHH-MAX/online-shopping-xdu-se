package com.shop.online.vo;

import lombok.Data;

@Data
public class SellerVO {
    private Long id;
    private Long userId;
    private String shopName;
    private String shopLogo;
    private String shopDesc;
    private Integer status;
} 