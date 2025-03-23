package com.shop.online.vo.category;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryVO {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 层级 1-一级 2-二级 3-三级
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    private Integer deleted;

    /**
     * 子分类列表
     */
    private List<CategoryVO> children;

    /**
     * 父分类对象
     */
    private CategoryVO parent;
} 