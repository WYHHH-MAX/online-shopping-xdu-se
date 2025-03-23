package com.shop.online.dto;

import lombok.Data;

/**
 * 卖家申请数据传输对象
 * 用于在后端和前端之间传输卖家申请相关数据
 */
@Data
public class SellerRequestDTO {
    /**
     * 申请ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 联系邮箱
     */
    private String email;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 申请理由/店铺描述
     */
    private String reason;
    
    /**
     * 申请状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer status;
    
    /**
     * 申请时间
     */
    private String applyTime;
    
    /**
     * 审核时间
     */
    private String reviewTime;
    
    /**
     * 审核人ID
     */
    private Long reviewerId;
    
    /**
     * 审核拒绝理由
     */
    private String reviewReason;
    
    /**
     * 店铺名称
     */
    private String shopName;
} 