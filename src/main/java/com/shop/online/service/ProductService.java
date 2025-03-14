package com.shop.online.service;

import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.vo.ProductVO;
import java.util.List;

public interface ProductService {
    PageResult<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size);
    
    PageResult<ProductVO> getProductsByCondition(ProductQueryDTO queryDTO);
    
    ProductVO getProductDetail(Long id);
    
    PageResult<ProductVO> getProductsBySeller(Long sellerId, Integer page, Integer size);

    PageResult<ProductVO> getFeaturedProducts(Integer page, Integer size);
} 