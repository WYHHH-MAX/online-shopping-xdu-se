package com.shop.online.controller;

import com.shop.online.common.result.PageResult;
import com.shop.online.common.result.Result;
import com.shop.online.dto.AdminStatsDTO;
import com.shop.online.dto.ProductDTO;
import com.shop.online.dto.SellerRequestDTO;
import com.shop.online.entity.Seller;
import com.shop.online.entity.User;
import com.shop.online.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取管理员统计数据
     */
    @GetMapping("/stats")
    public Result<AdminStatsDTO> getAdminStats() {
//        log.info("收到获取管理员统计数据请求");
        try {
            AdminStatsDTO stats = adminService.getAdminStats();
//            log.info("获取管理员统计数据成功: {}", stats);
            return Result.success(stats);
        } catch (Exception e) {
//            log.error("获取管理员统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理的卖家申请列表
     */
    @GetMapping("/seller/pending")
    public Result<List<SellerRequestDTO>> getPendingSellerRequests() {
//        log.info("收到获取待处理卖家申请请求");
        try {
            List<SellerRequestDTO> requests = adminService.getPendingSellerRequests();
//            log.info("获取待处理卖家申请成功，共有 {} 条记录", requests.size());
            log.debug("待处理申请详情: {}", requests);
            return Result.success(requests);
        } catch (Exception e) {
            log.error("获取待处理卖家申请失败: {}", e.getMessage(), e);
            return Result.error("获取待处理申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取卖家申请列表（分页）
     */
    @GetMapping("/seller/requests")
    public Result<PageResult<SellerRequestDTO>> getSellerRequests(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
//        log.info("收到获取卖家申请列表请求, 状态: {}, 页码: {}, 每页数量: {}", status, page, pageSize);
        try {
            PageResult<SellerRequestDTO> pageResult = adminService.getSellerRequests(status, page, pageSize);
//            log.info("获取卖家申请列表成功，总记录数: {}, 当前页数据量: {}",
//                     pageResult.getTotal(), pageResult.getList().size());
            log.debug("申请列表详情: {}", pageResult.getList());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取卖家申请列表失败: {}", e.getMessage(), e);
            return Result.error("获取申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 审批卖家申请 - 通过
     */
    @PostMapping("/seller/approve/{id}")
    public Result<Boolean> approveSellerRequest(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        log.info("收到通过卖家申请请求, 申请ID: {}, 管理员ID: {}", id, adminId);
        try {
            boolean result = adminService.approveSellerRequest(id, adminId);
            log.info("通过卖家申请成功, 申请ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("通过卖家申请失败, 申请ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("通过申请失败: " + e.getMessage());
        }
    }

    /**
     * 审批卖家申请 - 拒绝
     */
    @PostMapping("/seller/reject/{id}")
    public Result<Boolean> rejectSellerRequest(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam String reason) {
        log.info("收到拒绝卖家申请请求, 申请ID: {}, 管理员ID: {}, 拒绝理由: {}", id, adminId, reason);
        try {
            boolean result = adminService.rejectSellerRequest(id, adminId, reason);
            log.info("拒绝卖家申请成功, 申请ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("拒绝卖家申请失败, 申请ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("拒绝申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("收到获取用户列表请求, 用户名: {}, 角色: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                 username, role, status, page, pageSize);
        try {
            PageResult<User> pageResult = adminService.getUsers(username, role, status, page, pageSize);
            log.info("获取用户列表成功，总记录数: {}", pageResult.getTotal());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage(), e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态（启用/禁用）
     */
    @PostMapping("/user/status/{id}")
    public Result<Boolean> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("收到更新用户状态请求, 用户ID: {}, 新状态: {}", id, status);
        try {
            boolean result = adminService.updateUserStatus(id, status);
            log.info("更新用户状态成功, 用户ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新用户状态失败, 用户ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取商品列表（分页）
     */
    @GetMapping("/products")
    public Result<PageResult<ProductDTO>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("收到获取商品列表请求, 商品名称: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                 name, status, page, pageSize);
        try {
            PageResult<ProductDTO> pageResult = adminService.getProducts(name, status, page, pageSize);
            log.info("获取商品列表成功，总记录数: {}, 当前页数据量: {}", 
                    pageResult.getTotal(), pageResult.getList().size());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取商品列表失败: {}", e.getMessage(), e);
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/products/{id}")
    public Result<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("收到获取商品详情请求, 商品ID: {}", id);
        try {
            ProductDTO productDTO = adminService.getProductById(id);
            log.info("获取商品详情成功, 商品ID: {}", id);
            return Result.success(productDTO);
        } catch (Exception e) {
            log.error("获取商品详情失败, 商品ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品状态（上架/下架）
     */
    @PostMapping("/products/status/{id}")
    public Result<Boolean> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("收到更新商品状态请求, 商品ID: {}, 新状态: {}", id, status);
        try {
            boolean result = adminService.updateProductStatus(id, status);
            log.info("更新商品状态成功, 商品ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新商品状态失败, 商品ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("更新商品状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        log.info("收到删除商品请求, 商品ID: {}", id);
        try {
            boolean result = adminService.deleteProduct(id);
            log.info("删除商品成功, 商品ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除商品失败, 商品ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("删除商品失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有卖家列表（分页）
     */
    @GetMapping("/sellers")
    public Result<PageResult<Seller>> getAllSellers(
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("收到获取所有卖家列表请求, 店铺名称: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                 shopName, status, page, pageSize);
        try {
            PageResult<Seller> pageResult = adminService.getAllSellers(shopName, status, page, pageSize);
            log.info("获取卖家列表成功，总记录数: {}, 当前页数据量: {}", 
                    pageResult.getTotal(), pageResult.getList().size());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取卖家列表失败: {}", e.getMessage(), e);
            return Result.error("获取卖家列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新卖家状态
     */
    @PutMapping("/sellers/{id}/status")
    public Result<Boolean> updateSellerStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> requestBody) {
        Integer status = requestBody.get("status");
        log.info("收到更新卖家状态请求, 卖家ID: {}, 新状态: {}", id, status);
        try {
            boolean result = adminService.updateSellerStatus(id, status);
            log.info("更新卖家状态成功, 卖家ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新卖家状态失败, 卖家ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("更新卖家状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除卖家
     */
    @DeleteMapping("/sellers/{id}")
    public Result<Boolean> deleteSeller(@PathVariable Long id) {
        log.info("收到删除卖家请求, 卖家ID: {}", id);
        try {
            boolean result = adminService.deleteSeller(id);
            log.info("删除卖家成功, 卖家ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除卖家失败, 卖家ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("删除卖家失败: " + e.getMessage());
        }
    }

    /**
     * 设置商品为推荐/取消推荐
     */
    @PostMapping("/products/featured/{id}")
    public Result<Boolean> setProductFeatured(
            @PathVariable Long id,
            @RequestParam Integer featured) {
        log.info("收到设置商品推荐状态请求, 商品ID: {}, 推荐状态: {}", id, featured);
        try {
            boolean result = adminService.setProductFeatured(id, featured);
            log.info("设置商品推荐状态成功, 商品ID: {}", id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("设置商品推荐状态失败, 商品ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("设置商品推荐状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有订单列表（分页）
     */
    @GetMapping("/orders")
    public Result<?> getAllOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("收到获取所有订单列表请求, 订单号: {}, 用户名: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                 orderNo, username, status, page, pageSize);
        try {
            PageResult<?> pageResult = adminService.getAllOrders(orderNo, username, status, page, pageSize);
            log.info("获取所有订单列表成功，总记录数: {}", pageResult.getTotal());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取所有订单列表失败: {}", e.getMessage(), e);
            return Result.error("获取所有订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员发货接口
     */
    @PostMapping("/orders/{orderNo}/ship")
    public Result<Boolean> shipOrder(@PathVariable String orderNo) {
        log.info("收到管理员发货请求, 订单号: {}", orderNo);
        try {
            boolean result = adminService.shipOrder(orderNo);
            log.info("管理员发货成功, 订单号: {}", orderNo);
            return Result.success(result);
        } catch (Exception e) {
            log.error("管理员发货失败, 订单号: {}, 错误: {}", orderNo, e.getMessage(), e);
            return Result.error("发货失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{orderNo}")
    public Result<?> getOrderDetail(@PathVariable String orderNo) {
        log.info("收到获取订单详情请求, 订单号: {}", orderNo);
        try {
            Object orderDetail = adminService.getOrderDetail(orderNo);
            log.info("获取订单详情成功, 订单号: {}", orderNo);
            return Result.success(orderDetail);
        } catch (Exception e) {
            log.error("获取订单详情失败, 订单号: {}, 错误: {}", orderNo, e.getMessage(), e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }
} 