package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    /**
     * 根据用户ID查询购物车列表
     */
    List<Cart> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据ID查询购物车项
     */
    Cart selectById(Long id);
    
    /**
     * 根据用户ID和商品ID查询购物车项
     */
    Cart selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * 新增购物车项
     */
    int insert(Cart cart);
    
    /**
     * 更新购物车项
     */
    int update(Cart cart);
    
    /**
     * 根据ID删除购物车项
     */
    int deleteById(Long id);
    
    /**
     * 更新用户购物车商品的选中状态
     */
    int updateSelectedByUserId(@Param("userId") Long userId, @Param("selected") Integer selected);
    
    /**
     * 统计用户购物车商品数量
     */
    Integer countByUserId(Long userId);
    
    /**
     * 批量删除购物车项（MyBatis-Plus已经提供了deleteBatchIds方法，这里保留做为备用）
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 清空用户购物车
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 更新购物车项选中状态
     */
    int updateSelected(@Param("id") Long id, @Param("selected") Integer selected);
    
    /**
     * 批量更新购物车项选中状态
     */
    int updateSelectedBatch(@Param("userId") Long userId, @Param("selected") Integer selected);
} 