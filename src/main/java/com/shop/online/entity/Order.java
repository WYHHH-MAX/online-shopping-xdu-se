package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("`order`") // 防止order关键字冲突，需要加上引号
public class Order {

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态: 0-待付款，1-待发货，2-待收货，3-已完成，4-已取消
     */
    private Integer status;

    /**
     * 支付方式: 1-支付宝，2-微信支付，3-银行卡，4-货到付款
     */
    private Integer paymentMethod;

    /**
     * 收货人手机号码
     */
    private String phone;

    /**
     * 收货地址
     */
    private String location;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    /**
     * 支付时间
     */
    @TableField(exist = false)
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    @TableField(exist = false)
    private LocalDateTime shipTime;

    /**
     * 收货时间
     */
    @TableField(exist = false)
    private LocalDateTime receiveTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
} 