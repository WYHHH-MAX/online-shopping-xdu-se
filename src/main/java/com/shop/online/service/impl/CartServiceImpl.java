package com.shop.online.service.impl;

import com.shop.online.entity.Cart;
import com.shop.online.entity.Product;
import com.shop.online.entity.User;
import com.shop.online.mapper.CartMapper;
import com.shop.online.mapper.ProductMapper;
import com.shop.online.service.CartService;
import com.shop.online.service.UserService;
import com.shop.online.vo.cart.CartItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserService userService;

    @Override
    public List<CartItemVO> getUserCartItems() {
        // 获取当前登录用户
        User user = userService.getCurrentUser();
        if (user == null) {
            logger.warn("获取购物车列表时用户未登录");
            return new ArrayList<>();
        }

        // 查询该用户的购物车
        List<Cart> cartItems = cartMapper.selectByUserId(user.getId());
        
        // 转换为VO对象
        return cartItems.stream().map(cart -> {
            CartItemVO vo = new CartItemVO();
            vo.setId(cart.getId());
            vo.setUserId(cart.getUserId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setSelected(cart.getSelected() == 1);
            
            // 获取商品信息
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getMainImage());
                vo.setPrice(product.getPrice());
                vo.setStock(product.getStock());
            }
            
            vo.setCreateTime(cart.getCreatedTime());
            vo.setUpdateTime(cart.getUpdatedTime());
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addToCart(Long productId, Integer quantity) {
        try {
            // 参数校验
            if (productId == null || quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("无效的商品ID或数量");
            }
            
            // 获取当前登录用户
            User user = userService.getCurrentUser();
            if (user == null) {
                logger.error("添加购物车失败：用户未登录");
                throw new RuntimeException("用户未登录");
            }
            
            // 检查商品是否存在
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            
            // 检查商品库存
            if (product.getStock() < quantity) {
                throw new RuntimeException("商品库存不足");
            }
            
            // 检查购物车中是否已有该商品
            Cart existingItem = cartMapper.selectByUserIdAndProductId(user.getId(), productId);
            
            if (existingItem != null) {
                // 更新数量
                int newQuantity = existingItem.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    throw new RuntimeException("商品库存不足");
                }
                
                existingItem.setQuantity(newQuantity);
                existingItem.setUpdatedTime(LocalDateTime.now());
                cartMapper.update(existingItem);
                logger.info("已更新购物车商品: userId={}, productId={}, quantity={}", user.getId(), productId, newQuantity);
            } else {
                // 添加新商品到购物车
                Cart cart = new Cart();
                cart.setUserId(user.getId());
                cart.setProductId(productId);
                cart.setQuantity(quantity);
                cart.setSelected(1); // 默认选中
                cart.setCreatedTime(LocalDateTime.now());
                cart.setUpdatedTime(LocalDateTime.now());
                cart.setDeleted(0); // 未删除
                
                cartMapper.insert(cart);
                logger.info("已添加新商品到购物车: userId={}, productId={}, quantity={}", user.getId(), productId, quantity);
            }
        } catch (Exception e) {
            logger.error("添加商品到购物车失败: {}", e.getMessage());
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

    @Override
    @Transactional
    public void updateCartItem(Long id, Integer quantity) {
        if (id == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("无效的购物车项ID或数量");
        }
        
        // 获取当前登录用户
        User user = userService.getCurrentUser();
        if (user == null) {
            logger.error("更新购物车失败：用户未登录");
            throw new RuntimeException("用户未登录");
        }
        
        // 查询购物车项
        Cart cartItem = cartMapper.selectById(id);
        if (cartItem == null || !cartItem.getUserId().equals(user.getId())) {
            throw new RuntimeException("购物车项不存在");
        }
        
        // 检查商品库存
        Product product = productMapper.selectById(cartItem.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        // 更新数量
        cartItem.setQuantity(quantity);
        cartItem.setUpdatedTime(LocalDateTime.now());
        cartMapper.update(cartItem);
//        logger.info("已更新购物车商品数量: userId={}, cartItemId={}, quantity={}", user.getId(), id, quantity);
    }

    @Override
    @Transactional
    public void deleteCartItem(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("无效的购物车项ID");
        }
        
        // 获取当前登录用户
        User user = userService.getCurrentUser();
        if (user == null) {
            logger.error("删除购物车项失败：用户未登录");
            throw new RuntimeException("用户未登录");
        }
        
        // 查询购物车项
        Cart cartItem = cartMapper.selectById(id);
        if (cartItem == null || !cartItem.getUserId().equals(user.getId())) {
            throw new RuntimeException("购物车项不存在");
        }
        
        // 删除购物车项
        cartMapper.deleteById(id);
//        logger.info("已删除购物车商品: userId={}, cartItemId={}", user.getId(), id);
    }

    @Override
    @Transactional
    public void selectCartItem(Long id, Boolean selected) {
        // 获取当前登录用户
        User user = userService.getCurrentUser();
        if (user == null) {
            logger.warn("选择购物车项时用户未登录");
            return;
        }

        // 查询购物车项
        Cart cart = cartMapper.selectById(id);
        if (cart == null) {
            logger.warn("购物车项不存在, id={}", id);
            return;
        }

        // 验证所有权
        if (!user.getId().equals(cart.getUserId())) {
            logger.warn("用户{}尝试选择不属于自己的购物车项{}", user.getUsername(), id);
            return;
        }

        // 更新选中状态
        cart.setSelected(selected ? 1 : 0);
        cart.setUpdatedTime(LocalDateTime.now());
        cartMapper.update(cart);
    }
    
    @Override
    @Transactional
    public void selectCartItems(List<Integer> ids) {
        // 获取当前登录用户
        User user = userService.getCurrentUser();
        if (user == null) {
            logger.warn("批量选择购物车项时用户未登录");
            return;
        }
        
        if (ids == null || ids.isEmpty()) {
            logger.warn("批量选择购物车项参数为空");
            return;
        }
        
        // 将Integer列表转换为Long类型
        List<Long> longIds = ids.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        
        // 查询这些ID对应的购物车项
        // 由于没有selectByIds方法，我们需要查询每一个ID
        List<Cart> cartItems = new ArrayList<>();
        for (Long id : longIds) {
            Cart cart = cartMapper.selectById(id);
            if (cart != null && user.getId().equals(cart.getUserId())) {
                cartItems.add(cart);
            }
        }
        
        if (cartItems.isEmpty()) {
            logger.warn("未找到指定的购物车项");
            return;
        }
        
        // 更新选中状态为true
        for (Cart cart : cartItems) {
            cart.setSelected(1); // 1表示选中
            cart.setUpdatedTime(LocalDateTime.now());
            cartMapper.update(cart);
        }
        
        logger.info("用户{}批量选择了{}个购物车项", user.getUsername(), cartItems.size());
    }
    
    @Override
    @Transactional
    public void selectAllCartItems(Boolean selected) {
        if (selected == null) {
            throw new IllegalArgumentException("无效的选中状态");
        }
        
        // 获取当前登录用户
        User user = userService.getCurrentUser();
        if (user == null) {
            logger.error("全选/取消全选购物车失败：用户未登录");
            throw new RuntimeException("用户未登录");
        }
        
        // 更新该用户购物车中所有商品的选中状态
        cartMapper.updateSelectedByUserId(user.getId(), selected ? 1 : 0);
//        logger.info("已更新用户所有购物车商品选中状态: userId={}, selected={}", user.getId(), selected);
    }

    @Override
    public Integer getCartItemCount() {
        // 获取当前登录用户
        User user = userService.getCurrentUser();
        if (user == null) {
            return 0;
        }
        
        // 查询该用户的购物车商品总数量
        return cartMapper.countByUserId(user.getId());
    }
} 