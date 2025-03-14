package com.shop.online.dto.order;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PayOrderDTO {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
} 