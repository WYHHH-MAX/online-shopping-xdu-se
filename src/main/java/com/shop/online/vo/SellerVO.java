package com.shop.online.vo;

import lombok.Data;

@Data
public class SellerVO {
    private Long id;
    private Long userId;
    private String shopName;
    private String shopLogo;
    private String shopDesc;
    private Integer status;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String businessLicense;
    private String businessLicenseImage;
    private String idCardFront;
    private String idCardBack;
    private String wechatQrCode;
    private String alipayQrCode;
    private String rejectReason;
} 