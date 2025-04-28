package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 按商品类别统计销售数据
     * @param sellerId 卖家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售数据列表
     */
    @Select("SELECT c.name as category, SUM(oi.price * oi.quantity) as amount " +
            "FROM `order` o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "JOIN product p ON oi.product_id = p.id " +
            "JOIN category c ON p.category_id = c.id " +
            "WHERE o.seller_id = #{sellerId} " +
            "AND o.created_time >= #{startDate} " +
            "AND o.created_time <= #{endDate} " +
            "AND o.status = 3 " +
            "AND o.deleted = 0 " +
            "GROUP BY c.name " +
            "ORDER BY amount DESC")
    List<Map<String, Object>> getSalesByCategory(@Param("sellerId") Long sellerId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取热销商品
     * @param sellerId 卖家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 热销商品列表
     */
    @Select("SELECT p.id as productId, p.name as productName, " +
            "SUM(oi.price * oi.quantity) as salesAmount, " +
            "SUM(oi.quantity) as salesCount " +
            "FROM `order` o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "JOIN product p ON oi.product_id = p.id " +
            "WHERE o.seller_id = #{sellerId} " +
            "AND o.created_time >= #{startDate} " +
            "AND o.created_time <= #{endDate} " +
            "AND o.status = 3 " +
            "AND o.deleted = 0 " +
            "GROUP BY p.id, p.name " +
            "ORDER BY salesAmount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopSellingProducts(@Param("sellerId") Long sellerId, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate,
                                                  @Param("limit") Integer limit);
} 