package com.shop.online.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> list;
    public static <T> PageResult<T> of(long total, List<T> list) {
        return new PageResult<>(total, list);
    }

} 