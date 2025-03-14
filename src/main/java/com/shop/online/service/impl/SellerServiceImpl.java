package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.entity.Seller;
import com.shop.online.mapper.SellerMapper;
import com.shop.online.service.SellerService;
import com.shop.online.vo.SellerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SellerServiceImpl extends ServiceImpl<SellerMapper, Seller> implements SellerService {

    @Override
    public SellerVO getSellerByUserId(Long userId) {
        LambdaQueryWrapper<Seller> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seller::getUserId, userId);
        Seller seller = this.getOne(wrapper);
        return seller != null ? convertToVO(seller) : null;
    }

    @Override
    public SellerVO createSeller(Seller seller) {
        seller.setStatus(0);
        this.save(seller);
        return convertToVO(seller);
    }

    @Override
    public SellerVO updateSeller(Seller seller) {
        Seller existingSeller = this.getById(seller.getId());
        if (existingSeller == null) {
            return null;
        }
        BeanUtils.copyProperties(seller, existingSeller, "id", "userId", "createdTime", "deleted");
        this.updateById(existingSeller);
        return convertToVO(existingSeller);
    }

    private SellerVO convertToVO(Seller seller) {
        SellerVO vo = new SellerVO();
        BeanUtils.copyProperties(seller, vo);
        return vo;
    }
} 