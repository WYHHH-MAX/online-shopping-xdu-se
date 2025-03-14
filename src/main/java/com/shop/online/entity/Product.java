package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
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

    private Boolean isFeatured;

    private Integer featuredSort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<String> images;
} 