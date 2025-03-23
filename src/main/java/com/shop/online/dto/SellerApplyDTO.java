package com.shop.online.dto;

import lombok.Data;

/**
 * 商家入驻申请DTO
 */
@Data
public class SellerApplyDTO {
    /**
     * 用户名 - 新用户注册时使用
     */
    private String username;
    
    /**
     * 密码 - 新用户注册时使用
     */
    private String password;
    
    /**
     * 昵称 - 新用户注册时使用
     */
    private String nickname;
    
    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 营业执照号
     */
    private String businessLicense;
} 