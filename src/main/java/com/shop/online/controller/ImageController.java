package com.shop.online.controller;

import com.shop.online.entity.Seller;
import com.shop.online.service.ProductService;
import com.shop.online.service.SellerService;
import com.shop.online.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 图片控制器
 */
@RestController
@RequestMapping("/api/images")
@Slf4j
public class ImageController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerService sellerService;

    /**
     * 删除商品图片
     * @param productId 商品ID
     * @param imageUrl 图片URL
     * @return 操作结果
     */
    @DeleteMapping("/product")
    public Result<Boolean> deleteProductImage(
            @RequestParam Long productId,
            @RequestParam String imageUrl) {
        
        log.info("删除商品图片请求: productId={}, imageUrl={}", productId, imageUrl);
        
        try {
            // 获取当前商家
            Seller seller = sellerService.getCurrentSeller();
            
            // 验证权限 - 确保只能删除自己商品的图片
            if (!productService.isProductOwnedBySeller(productId, seller.getId())) {
                log.error("无权删除此商品图片, productId={}, sellerId={}", productId, seller.getId());
                return Result.error("无权删除此商品图片");
            }
            
            // 执行删除
            boolean deleted = productService.deleteProductImage(productId, imageUrl);
            
            if (deleted) {
                log.info("商品图片删除成功: productId={}, imageUrl={}", productId, imageUrl);
                return Result.success(true);
            } else {
                log.warn("商品图片删除失败: productId={}, imageUrl={}", productId, imageUrl);
                return Result.error("图片删除失败，图片可能不存在");
            }
        } catch (Exception e) {
            log.error("删除商品图片失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除商品的所有图片
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/product/all")
    public Result<Boolean> deleteAllProductImages(@RequestParam Long productId) {
        log.info("批量删除商品所有图片请求: productId={}", productId);
        
        try {
            // 获取当前商家
            Seller seller = sellerService.getCurrentSeller();
            
            // 验证权限 - 确保只能删除自己商品的图片
            if (!productService.isProductOwnedBySeller(productId, seller.getId())) {
                log.error("无权删除此商品图片, productId={}, sellerId={}", productId, seller.getId());
                return Result.error("无权删除此商品图片");
            }
            
            // 执行批量删除
            boolean deleted = productService.deleteAllProductImages(productId);
            
            if (deleted) {
                log.info("批量删除商品图片成功: productId={}", productId);
                return Result.success(true);
            } else {
                log.warn("批量删除商品图片失败: productId={}", productId);
                return Result.error("批量删除图片失败");
            }
        } catch (Exception e) {
            log.error("批量删除商品图片失败", e);
            return Result.error(e.getMessage());
        }
    }
} 