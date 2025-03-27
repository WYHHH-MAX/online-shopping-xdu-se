package com.shop.online.controller;

import com.shop.online.dto.cart.CartItemDTO;
import com.shop.online.service.CartService;
import com.shop.online.vo.Result;
import com.shop.online.vo.cart.CartItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.getUserCartItems());
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody CartItemDTO cartItemDTO) {
//        logger.info("接收到添加购物车请求: {}", cartItemDTO);
        if (cartItemDTO.getProductId() == null) {
            return Result.error("商品ID不能为空");
        }
        if (cartItemDTO.getQuantity() == null || cartItemDTO.getQuantity() <= 0) {
            // 如果数量为空或小于等于0，默认设置为1
            cartItemDTO.setQuantity(1);
        }
        
        try {
            cartService.addToCart(cartItemDTO.getProductId(), cartItemDTO.getQuantity());
            return Result.success();
        } catch (Exception e) {
            logger.error("添加购物车失败", e);
            return Result.error("添加购物车失败: " + e.getMessage());
        }
    }
    
    /**
     * 直接添加商品到购物车（接受简单参数）
     */
    @PostMapping("/quickAdd")
    public Result<Void> quickAdd(@RequestBody Map<String, Object> params) {
//        logger.info("接收到快速添加购物车请求: {}", params);
        
        Long productId = null;
        Integer quantity = 1;
        
        try {
            if (params.containsKey("productId")) {
                productId = Long.valueOf(params.get("productId").toString());
            }
            
            if (params.containsKey("quantity")) {
                quantity = Integer.valueOf(params.get("quantity").toString());
            }
            
            if (productId == null) {
                return Result.error("商品ID不能为空");
            }
            
            cartService.addToCart(productId, quantity);
            return Result.success();
        } catch (NumberFormatException e) {
            logger.error("参数格式错误", e);
            return Result.error("参数格式错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("添加购物车失败", e);
            return Result.error("添加购物车失败: " + e.getMessage());
        }
    }

    /**
     * 更新购物车
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody CartItemDTO cartItemDTO) {
        cartService.updateCartItem(cartItemDTO.getId(), cartItemDTO.getQuantity());
        return Result.success();
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.deleteCartItem(id);
        return Result.success();
    }

    /**
     * 选择/取消选择商品
     */
    @PutMapping("/select")
    public Result<Void> select(@RequestBody CartItemDTO cartItemDTO) {
        cartService.selectCartItem(cartItemDTO.getId(), cartItemDTO.getSelected());
        return Result.success();
    }
    
    /**
     * 批量选择购物车项
     */
    @PostMapping("/selectBatch")
    public Result<Void> selectBatch(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> ids = params.get("ids");
        if (ids != null && !ids.isEmpty()) {
            cartService.selectCartItems(ids);
        }
        return Result.success();
    }

    /**
     * 全选/取消全选
     */
    @PutMapping("/selectAll")
    public Result<Void> selectAll(@RequestBody CartItemDTO cartItemDTO) {
        cartService.selectAllCartItems(cartItemDTO.getSelected());
        return Result.success();
    }

    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.success(cartService.getCartItemCount());
    }
} 