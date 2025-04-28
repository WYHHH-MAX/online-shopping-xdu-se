package com.shop.online.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.online.common.result.PageResult;
import com.shop.online.dto.SellerApplyDTO;
import com.shop.online.entity.Seller;
import com.shop.online.vo.SellerVO;

/**
 * 商家服务接口
 */
public interface SellerService extends IService<Seller> {

    /**
     * 商家入驻申请
     * @param sellerApplyDTO 商家入驻申请参数
     * @return 申请结果
     */
    boolean apply(SellerApplyDTO sellerApplyDTO);

    /**
     * 获取商家详情
     * @param sellerId 商家ID
     * @return 商家详情
     */
    Seller getSellerInfo(Long sellerId);

    /**
     * 审核商家入驻申请
     * @param sellerId 商家ID
     * @param approved 是否通过
     * @param rejectReason 拒绝原因
     * @return 审核结果
     */
    boolean audit(Long sellerId, boolean approved, String rejectReason);

    /**
     * 获取当前登录的商家信息
     * @return 商家信息
     */
    Seller getCurrentSeller();

    /**
     * 获取所有待审核的商家列表
     * @param page 页码
     * @param size 每页大小
     * @return 待审核商家列表
     */
    PageResult<Seller> getPendingSellers(int page, int size);

    /**
     * 上传商家资质图片
     * @param sellerId 商家ID
     * @param fileType 文件类型(logo, businessLicenseImage, idCardFront, idCardBack)
     * @param filePath 文件路径
     * @return 更新结果
     */
    boolean uploadQualification(Long sellerId, String fileType, String filePath);

    /**
     * 上传支付二维码
     * @param sellerId 商家ID
     * @param payType 支付类型(wechat, alipay)
     * @param filePath 文件路径
     * @return 更新结果
     */
    boolean uploadPaymentQrCode(Long sellerId, String payType, String filePath);

    SellerVO getSellerByUserId(Long userId);
    SellerVO createSeller(Seller seller);
    SellerVO updateSeller(Seller seller);
} 