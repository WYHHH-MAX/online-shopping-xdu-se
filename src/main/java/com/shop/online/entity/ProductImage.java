package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_image")
public class ProductImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String imageUrl;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
} 