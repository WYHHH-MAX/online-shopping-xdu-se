package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 统计商品总数
     * @return 商品总数
     */
    @Select("SELECT COUNT(*) FROM product WHERE status = 1")
    Long countProducts();
} 