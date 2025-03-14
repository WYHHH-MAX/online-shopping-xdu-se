package com.shop.online.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductQueryDTO {
    private Long categoryId;
    private String keyword;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer page = 1;
    private Integer size = 10;
    private String orderBy;
    private Boolean isAsc;
} 