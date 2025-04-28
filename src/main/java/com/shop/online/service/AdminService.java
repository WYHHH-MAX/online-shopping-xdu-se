package com.shop.online.service;

import com.shop.online.common.result.PageResult;
import com.shop.online.dto.AdminStatsDTO;
import com.shop.online.dto.ProductDTO;
import com.shop.online.dto.SellerRequestDTO;
import com.shop.online.entity.User;
import com.shop.online.entity.Seller;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 获取管理员统计数据
     */
    AdminStatsDTO getAdminStats();

    /**
     * 获取待处理的卖家申请
     */
    List<SellerRequestDTO> getPendingSellerRequests();

    /**
     * 获取卖家申请列表（分页）
     */
    PageResult<SellerRequestDTO> getSellerRequests(Integer status, int page, int pageSize);

    /**
     * 审批卖家申请 - 通过
     */
    boolean approveSellerRequest(Long id, Long adminId);

    /**
     * 审批卖家申请 - 拒绝
     */
    boolean rejectSellerRequest(Long id, Long adminId, String reason);

    /**
     * 获取用户列表（分页）
     */
    PageResult<User> getUsers(String username, Integer role, Integer status, int page, int pageSize);

    /**
     * 更新用户状态（启用/禁用）
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 获取商品列表
     */
    PageResult<ProductDTO> getProducts(String name, Integer status, int page, int pageSize);

    /**
     * 获取商品详情
     */
    ProductDTO getProductById(Long id);

    /**
     * 更新商品状态
     */
    boolean updateProductStatus(Long id, Integer status);

    /**
     * 删除商品
     */
    boolean deleteProduct(Long id);

    /**
     * 获取所有卖家列表（分页）
     */
    PageResult<Seller> getAllSellers(String shopName, Integer status, int page, int pageSize);

    /**
     * 更新卖家状态
     */
    boolean updateSellerStatus(Long id, Integer status);

    /**
     * 删除卖家
     */
    boolean deleteSeller(Long id);

    /**
     * 设置商品为推荐/取消推荐
     * @param id 商品ID
     * @param featured 推荐状态：1-推荐，0-取消推荐
     * @return 是否操作成功
     */
    boolean setProductFeatured(Long id, Integer featured);

    /**
     * 获取所有订单列表（分页）
     * @param orderNo 订单号
     * @param username 用户名
     * @param status 订单状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 订单分页结果
     */
    PageResult<?> getAllOrders(String orderNo, String username, Integer status, int page, int pageSize);

    /**
     * 管理员发货
     * @param orderNo 订单号
     * @return 是否操作成功
     */
    boolean shipOrder(String orderNo);

    /**
     * 获取订单详情
     */
    Object getOrderDetail(String orderNo);
} 