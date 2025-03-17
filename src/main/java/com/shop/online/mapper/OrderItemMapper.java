package com.shop.online.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.online.dto.OrderItemDTO;
import com.shop.online.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单明细Mapper接口
 */
@Mapper
public interface OrderItemMapper {
    /**
     * 批量插入订单明细
     * 
     * @param orderItems 订单明细列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<OrderItemDTO> orderItems);
    
    /**
     * 根据订单ID查询订单明细
     * 
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId} AND deleted = 0")
    List<OrderItemDTO> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单ID列表查询订单明细
     * 
     * @param orderIds 订单ID列表
     * @return 订单明细列表
     */
    @Select("<script>" +
            "SELECT * FROM order_item WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    List<OrderItemDTO> selectByOrderIds(@Param("orderIds") List<Long> orderIds);
    
    /**
     * 根据ID查询订单明细
     * 
     * @param id 明细ID
     * @return 订单明细
     */
    @Select("SELECT * FROM order_item WHERE id = #{id} AND deleted = 0")
    OrderItemDTO selectById(@Param("id") Long id);
    
    /**
     * 逻辑删除订单明细
     * 
     * @param id 明细ID
     * @return 影响行数
     */
    @Update("UPDATE order_item SET deleted = 1 WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
} 