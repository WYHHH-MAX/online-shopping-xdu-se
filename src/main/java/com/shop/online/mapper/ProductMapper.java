package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 统计商品总数
     * @return 商品总数
     */
    @Select("SELECT COUNT(*) FROM product WHERE status = 1")
    Long countProducts();
    
    /**
     * 判断是否为一级分类
     * @param categoryId 分类ID
     * @return 是否为一级分类
     */
    @Select("SELECT COUNT(*) FROM category WHERE id = #{categoryId} AND parent_id = 0")
    Integer isCategoryPrimary(Long categoryId);
    
    /**
     * 获取指定分类的所有子分类ID
     * @param parentId 父分类ID
     * @return 子分类ID列表
     */
    @Select("SELECT id FROM category WHERE parent_id = #{parentId}")
    List<Long> getSubCategoryIds(Long parentId);
} 