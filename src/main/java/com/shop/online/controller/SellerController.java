package com.shop.online.controller;

import com.shop.online.common.PageResult;
import com.shop.online.dto.ProductDTO;
import com.shop.online.dto.SellerApplyDTO;
import com.shop.online.entity.Order;
import com.shop.online.entity.Product;
import com.shop.online.entity.Seller;
import com.shop.online.service.OrderService;
import com.shop.online.service.ProductService;
import com.shop.online.service.SellerService;
import com.shop.online.util.FileUtil;
import com.shop.online.vo.OrderVO;
import com.shop.online.vo.ProductVO;
import com.shop.online.vo.Result;
import com.shop.online.vo.SellerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家控制器
 */
@RestController
@RequestMapping("/seller")
@Slf4j
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;

    /**
     * 商家入驻申请
     */
    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody SellerApplyDTO sellerApplyDTO) {
        log.info("商家入驻申请: {}", sellerApplyDTO);
        try {
            // 日志记录，隐藏密码
            boolean isNewUser = StringUtils.hasText(sellerApplyDTO.getUsername()) && StringUtils.hasText(sellerApplyDTO.getPassword());
            log.info("接收到{}用户的商家入驻申请, username={}, shopName={}", 
                    isNewUser ? "未登录" : "已登录",
                    sellerApplyDTO.getUsername(),
                    sellerApplyDTO.getShopName());
            
            boolean result = sellerService.apply(sellerApplyDTO);
            log.info("商家入驻申请处理结果: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("商家入驻申请失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 商家资质图片上传
     */
    @PostMapping("/upload/{fileType}")
    public Result<String> uploadQualification(
            @PathVariable String fileType,
            @RequestParam("file") MultipartFile file) {
        log.info("上传商家资质图片, 类型: {}, 文件名: {}, 大小: {}", fileType, file.getOriginalFilename(), file.getSize());
        
        try {
            // 获取当前商家
            Seller currentSeller = sellerService.getCurrentSeller();
            
            // 上传文件并获取路径
            String filePath = FileUtil.uploadSellerQualification(file, currentSeller.getId(), fileType);
            
            // 更新商家资质信息
            sellerService.uploadQualification(currentSeller.getId(), fileType, filePath);
            
            // 返回完整URL
            String fullUrl = "/api" + filePath;
            log.info("资质图片上传成功，路径: {}", fullUrl);
            
            return Result.success(fullUrl);
        } catch (IOException e) {
            log.error("资质图片上传失败", e);
            return Result.error("资质图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取商家信息
     */
    @GetMapping("/info")
    public Result<Seller> getSellerInfo() {
        log.info("获取商家信息");
        try {
            Seller seller = sellerService.getCurrentSeller();
            return Result.success(seller);
        } catch (Exception e) {
            log.error("获取商家信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商家商品列表
     */
    @GetMapping("/products")
    public Result<PageResult<ProductVO>> getSellerProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取商家商品列表, page={}, size={}", page, size);
        try {
            Seller seller = sellerService.getCurrentSeller();
            PageResult<ProductVO> products = productService.getProductsBySeller(seller.getId(), page, size);
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取商家商品列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("/products")
    public Result<ProductVO> addProduct(@RequestBody ProductDTO productDTO) {
        log.info("添加商品: {}", productDTO);
        try {
            Seller seller = sellerService.getCurrentSeller();
            // 转换DTO为实体
            Product product = new Product();
            // 设置卖家ID
            product.setSellerId(seller.getId());
            // 保存商品
            ProductVO productVO = productService.saveProduct(product);
            return Result.success(productVO);
        } catch (Exception e) {
            log.error("添加商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        log.info("更新商品: id={}, dto={}", id, productDTO);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 检查商品是否属于当前卖家
            ProductVO existingProduct = productService.getProductDetail(id);
            if (existingProduct == null) {
                return Result.error("商品不存在");
            }
            
            if (!existingProduct.getSellerId().equals(seller.getId())) {
                return Result.error("无权操作此商品");
            }
            
            // 更新商品
            Product product = new Product();
            product.setId(id);
            // 保留卖家ID不变
            product.setSellerId(seller.getId());
            // 更新商品
            ProductVO productVO = productService.updateProduct(product);
            return Result.success(productVO);
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        log.info("删除商品: id={}", id);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 检查商品是否属于当前卖家
            ProductVO existingProduct = productService.getProductDetail(id);
            if (existingProduct == null) {
                return Result.error("商品不存在");
            }
            
            if (!existingProduct.getSellerId().equals(seller.getId())) {
                return Result.error("无权操作此商品");
            }
            
            // 删除商品
            boolean deleted = productService.deleteProduct(id);
            
            return Result.success(deleted);
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/products/image")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        log.info("上传商品图片, 文件名: {}, 大小: {}", file.getOriginalFilename(), file.getSize());
        
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 上传文件并获取路径
            String filePath = FileUtil.uploadProductImage(file, seller.getId());
            
            // 返回完整URL
            String fullUrl = "/api" + filePath;
            log.info("商品图片上传成功，路径: {}", fullUrl);
            
            return Result.success(fullUrl);
        } catch (IOException e) {
            log.error("商品图片上传失败", e);
            return Result.error("商品图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取卖家订单列表
     */
    @GetMapping("/orders")
    public Result<PageResult<OrderVO>> getSellerOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取卖家订单列表, status={}, page={}, size={}", status, page, size);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("sellerId", seller.getId());
            if (status != null) {
                params.put("status", status);
            }
            params.put("page", page);
            params.put("size", size);
            
            // 查询订单列表
            PageResult<OrderVO> orders = orderService.getSellerOrders(params);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取卖家订单列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发货
     */
    @PostMapping("/orders/{orderNo}/ship")
    public Result<Boolean> shipOrder(@PathVariable String orderNo) {
        log.info("卖家发货: orderNo={}", orderNo);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 发货
            boolean shipped = orderService.shipOrder(orderNo, seller.getId());
            return Result.success(shipped);
        } catch (Exception e) {
            log.error("卖家发货失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 卖家统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getSellerDashboard() {
        log.info("获取卖家统计数据");
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 构建统计结果
            Map<String, Object> result = new HashMap<>();
            
            // 获取各状态订单数量
            // 待发货订单数
            int pendingShipments = orderService.countSellerOrdersByStatus(seller.getId(), 1);
            // 总订单数
            int totalOrders = orderService.countSellerOrders(seller.getId());
            // 商品总数
            int totalProducts = productService.countSellerProducts(seller.getId());
            // 库存不足商品数
            int lowStockProducts = productService.countSellerLowStockProducts(seller.getId(), 10);
            
            result.put("pendingShipments", pendingShipments);
            result.put("totalOrders", totalOrders);
            result.put("totalProducts", totalProducts);
            result.put("lowStockProducts", lowStockProducts);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取卖家统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品库存
     */
    @PutMapping("/products/{id}/stock")
    public Result<Boolean> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        log.info("更新商品库存: id={}, stock={}", id, stock);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 检查商品是否属于当前卖家
            ProductVO existingProduct = productService.getProductDetail(id);
            if (existingProduct == null) {
                return Result.error("商品不存在");
            }
            
            if (!existingProduct.getSellerId().equals(seller.getId())) {
                return Result.error("无权操作此商品");
            }
            
            // 更新库存
            boolean updated = productService.updateStock(id, stock);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新商品库存失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新商品库存
     */
    @PutMapping("/products/stock/batch")
    public Result<Boolean> batchUpdateProductStock(@RequestBody Map<Long, Integer> stockMap) {
        log.info("批量更新商品库存: {}", stockMap);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 批量更新库存
            boolean updated = productService.batchUpdateStock(seller.getId(), stockMap);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("批量更新商品库存失败", e);
            return Result.error(e.getMessage());
        }
    }
} 