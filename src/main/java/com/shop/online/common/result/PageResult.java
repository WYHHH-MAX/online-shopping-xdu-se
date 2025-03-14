package com.shop.online.common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private List<T> list;

    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }
} 