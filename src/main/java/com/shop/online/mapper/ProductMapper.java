package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
} 