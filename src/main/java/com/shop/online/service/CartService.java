package com.shop.online.service;

import com.shop.online.vo.cart.CartItemVO;

import java.util.List;

public interface CartService {
    /**
     * 获取当前用户的购物车列表
     */
    List<CartItemVO> getUserCartItems();

    /**
     * 添加商品到购物车
     */
    void addToCart(Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     */
    void updateCartItem(Long id, Integer quantity);

    /**
     * 删除购物车商品
     */
    void deleteCartItem(Long id);

    /**
     * 选择/取消选择购物车商品
     */
    void selectCartItem(Long id, Boolean selected);

    /**
     * 批量选择购物车项
     */
    void selectCartItems(List<Integer> ids);

    /**
     * 全选/取消全选购物车商品
     */
    void selectAllCartItems(Boolean selected);

    /**
     * 获取购物车商品数量
     */
    Integer getCartItemCount();
} 