package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体类
 */
@Data
@TableName("product")
public class Product {

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 状态 0-下架 1-上架
     */
    private Integer status;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 主图
     */
    private String mainImage;

    /**
     * 是否推荐 0-否 1-是
     */
    private Integer isFeatured;

    /**
     * 推荐排序
     */
    private Integer featuredSort;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<String> images;
} 