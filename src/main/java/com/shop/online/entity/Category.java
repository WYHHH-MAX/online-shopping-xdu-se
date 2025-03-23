package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Long parentId;
    
    private Integer level;
    
    private Integer sort;
    
    // 图标URL
    private String icon;
    
    // 0-禁用 1-启用
    // Temporarily commented out due to missing column in database
    // private Integer status = 1;
    
    @TableField(exist = false)
    private List<Category> children;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableLogic
    private Integer deleted;
} 