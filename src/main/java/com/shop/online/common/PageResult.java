package com.shop.online.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页数据
     */
    private List<T> list;
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(long total, List<T> list) {
        return new PageResult<>(total, list);
    }
} 