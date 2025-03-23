package com.shop.online.dto;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private Long userCount;
    private Long sellerCount;
    private Long pendingRequests;
    private Long productCount;
} 