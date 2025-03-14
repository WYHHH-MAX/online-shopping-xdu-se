package com.shop.online.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVO {
    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private Integer sales;
    private String mainImage;
    private List<String> images;  // 商品的所有图片URL列表
} 