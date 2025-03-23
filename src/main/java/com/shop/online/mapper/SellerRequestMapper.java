package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.entity.Seller;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SellerRequestMapper extends BaseMapper<Seller> {
    
    /**
     * 统计待处理申请数量
     * @return 待处理申请数量
     */
    @Select("SELECT COUNT(*) FROM seller WHERE status = 0")
    Long countPendingRequests();
} 