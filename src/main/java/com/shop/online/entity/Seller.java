package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seller")
public class Seller {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String shopName;

    private String shopLogo;

    private String shopDesc;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
} 