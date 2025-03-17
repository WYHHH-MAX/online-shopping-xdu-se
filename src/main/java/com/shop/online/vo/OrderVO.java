package com.shop.online.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
 */
@Data
public class OrderVO {
    
    /**
     * 订单ID
     */
    private Integer id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 订单状态: 0-待付款，1-待发货，2-待收货，3-已完成，4-已取消
     */
    private String status;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 订单商品列表
     */
    private List<OrderProductVO> products;
    
    /**
     * 订单商品VO
     */
    @Data
    public static class OrderProductVO {
        /**
         * 商品ID
         */
        private Integer id;
        
        /**
         * 商品名称
         */
        private String name;
        
        /**
         * 商品图片
         */
        private String image;
        
        /**
         * 商品价格
         */
        private BigDecimal price;
        
        /**
         * 商品数量
         */
        private Integer quantity;
    }
} 