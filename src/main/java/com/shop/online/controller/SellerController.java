package com.shop.online.controller;

import com.shop.online.entity.Seller;
import com.shop.online.vo.SellerVO;
import com.shop.online.service.SellerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Resource
    private SellerService sellerService;

    @GetMapping("/{userId}")
    public SellerVO getSellerByUserId(@PathVariable Long userId) {
        return sellerService.getSellerByUserId(userId);
    }

    @PostMapping
    public SellerVO createSeller(@RequestBody Seller seller) {
        return sellerService.createSeller(seller);
    }

    @PutMapping
    public SellerVO updateSeller(@RequestBody Seller seller) {
        return sellerService.updateSeller(seller);
    }
} 