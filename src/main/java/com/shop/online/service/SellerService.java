package com.shop.online.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.online.entity.Seller;
import com.shop.online.vo.SellerVO;

public interface SellerService extends IService<Seller> {
    SellerVO getSellerByUserId(Long userId);
    SellerVO createSeller(Seller seller);
    SellerVO updateSeller(Seller seller);
} 