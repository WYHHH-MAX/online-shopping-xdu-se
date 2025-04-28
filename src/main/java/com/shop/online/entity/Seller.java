package com.shop.online.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家入驻信息实体类
 */
@Data
@TableName("seller")
public class Seller {

    /**
     * 商家ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺Logo
     */
    @TableField("shop_logo")
    private String shopLogo;

    /**
     * 店铺描述
     */
    @TableField("shop_desc")
    private String shopDesc;

    /**
     * 审核状态: 0-待审核，1-审核通过，2-审核拒绝
     */
    private Integer status;

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

    /**
     * 营业执照图片
     */
    private String businessLicenseImage;

    /**
     * 身份证正面照片
     */
    private String idCardFront;

    /**
     * 身份证背面照片
     */
    private String idCardBack;

    /**
     * 微信支付二维码
     */
    @TableField("wechat_qr_code")
    private String wechatQrCode;

    /**
     * 支付宝支付二维码
     */
    @TableField("alipay_qr_code")
    private String alipayQrCode;

    /**
     * 审核拒绝理由
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
} 