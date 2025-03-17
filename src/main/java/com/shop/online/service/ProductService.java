package com.shop.online.service;

import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.vo.ProductVO;
import java.util.List;

public interface ProductService {
    /**
     * 根据分类ID获取商品列表
     */
    PageResult<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size);
    
    /**
     * 根据条件查询商品
     */
    PageResult<ProductVO> getProductsByCondition(ProductQueryDTO queryDTO);
    
    /**
     * 获取商品详情
     */
    ProductVO getProductDetail(Long id);
    
    /**
     * 获取卖家的商品列表
     */
    PageResult<ProductVO> getProductsBySeller(Long sellerId, Integer page, Integer size);

    /**
     * 获取推荐商品列表
     */
    PageResult<ProductVO> getFeaturedProducts(Integer page, Integer size);

    /**
     * 根据分类ID获取商品列表(包含排序)
     */
    PageResult<ProductVO> getProductsByCategoryWithSort(Long categoryId, Integer page, Integer size, String sortBy);

    /**
     * 根据分类ID获取商品列表(分类ID为Integer类型)
     */
    PageResult<ProductVO> getProductsByCategory(Integer categoryId, Integer page, Integer size, String sortBy);
} 