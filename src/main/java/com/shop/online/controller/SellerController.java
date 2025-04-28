package com.shop.online.controller;

import com.shop.online.common.result.PageResult;
import com.shop.online.vo.Result;
import com.shop.online.dto.ProductDTO;
import com.shop.online.dto.SellerApplyDTO;
import com.shop.online.entity.Order;
import com.shop.online.entity.Product;
import com.shop.online.entity.Seller;
import com.shop.online.exception.BusinessException;
import com.shop.online.service.OrderService;
import com.shop.online.service.ProductService;
import com.shop.online.service.SellerService;
import com.shop.online.service.UserService;
import com.shop.online.util.FileUtil;
import com.shop.online.vo.OrderVO;
import com.shop.online.vo.ProductVO;
import com.shop.online.vo.SellerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
            log.info("当前商家ID: {}", currentSeller.getId());
            
            // 上传文件并获取路径 - 这个路径已经是 /images/seller/filename 格式，不含/api前缀
            String relativePath = FileUtil.uploadSellerQualification(file, currentSeller.getId(), fileType);
            log.info("资质图片上传结果 - 相对路径: {}", relativePath);
            
            // 更新商家资质信息 - 保存到数据库的路径不含/api前缀
            sellerService.uploadQualification(currentSeller.getId(), fileType, relativePath);
            log.info("商家资质信息已更新, 资质类型: {}, 路径: {}", fileType, relativePath);
            
            // 返回相对路径，前端会自动添加基础URL
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("资质图片上传失败: {}", e.getMessage(), e);
            return Result.error("资质图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 支付二维码上传
     */
    @PostMapping("/upload/payment-qrcode")
    public Result<String> uploadPaymentQrCode(
            @RequestParam("file") MultipartFile file,
            @RequestParam("payType") String payType) {
        log.info("上传支付二维码, 类型: {}, 文件名: {}, 大小: {}", payType, file.getOriginalFilename(), file.getSize());
        
        try {
            // 获取当前商家
            Seller currentSeller = sellerService.getCurrentSeller();
            log.info("当前商家ID: {}", currentSeller.getId());
            
            // 上传文件并获取路径
            String relativePath = FileUtil.uploadPaymentQrCode(file, currentSeller.getId(), payType);
            log.info("支付二维码上传结果 - 相对路径: {}", relativePath);
            
            // 更新商家支付二维码信息
            sellerService.uploadPaymentQrCode(currentSeller.getId(), payType, relativePath);
            log.info("商家支付二维码已更新, 支付类型: {}, 路径: {}", payType, relativePath);
            
            // 返回相对路径，前端会自动添加基础URL
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("支付二维码上传失败: {}", e.getMessage(), e);
            return Result.error("支付二维码上传失败: " + e.getMessage());
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
        log.info("添加商品请求数据: {}", productDTO);
        try {
            // 验证必要字段
            if (productDTO.getCategoryId() == null) {
                return Result.error("商品分类不能为空");
            }
            
            // 验证图片数据
            if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
                log.info("商品图片数量: {}", productDTO.getImages().size());
                // 设置第一张图片为主图
                productDTO.setMainImage(productDTO.getImages().get(0));
            } else {
                return Result.error("请至少上传一张商品图片");
            }
            
            Seller seller = sellerService.getCurrentSeller();
            // 转换DTO为实体
            Product product = new Product();
            BeanUtils.copyProperties(productDTO, product);
            // 设置卖家ID
            product.setSellerId(seller.getId());
            // 保存商品
            ProductVO productVO = productService.saveProduct(product);
            log.info("商品添加成功: {}", productVO);
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
        log.info("更新商品请求数据: id={}, dto={}", id, productDTO);
        try {
            // 验证必要字段
            if (productDTO.getCategoryId() == null) {
                return Result.error("商品分类不能为空");
            }
            
            // 验证图片数据
            if (productDTO.getImages() != null) {
                log.info("商品图片数量: {}", productDTO.getImages().size());
                for (String image : productDTO.getImages()) {
                    log.debug("图片URL: {}", image);
                }
            }
            
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
            BeanUtils.copyProperties(productDTO, product);
            product.setId(id);
            // 保留卖家ID不变
            product.setSellerId(seller.getId());
            // 更新商品
            ProductVO productVO = productService.updateProduct(product);
            log.info("商品更新成功: {}", productVO);
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
    public Result<String> uploadProductImage(
            @RequestParam("file") MultipartFile file, 
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer imageIndex) {
        log.info("上传商品图片, 文件名: {}, 大小: {}, 商品ID: {}, 图片序号: {}", 
                 file.getOriginalFilename(), file.getSize(), productId, imageIndex);
        
        try {
            Seller seller = sellerService.getCurrentSeller();
            log.info("当前商家ID: {}", seller.getId());
            
            // 如果没有提供图片序号但提供了商品ID，则获取当前图片数量
            if (productId != null && imageIndex == null) {
                int currentImageCount = productService.getProductImageCount(productId);
                // 新图片的序号从当前图片数量+1开始
                imageIndex = currentImageCount + 1;
                log.info("自动计算图片序号: 商品{}的当前图片数量={}, 新序号={}", 
                          productId, currentImageCount, imageIndex);
            }
            
            // 使用FileUtil上传图片，提供更全面的参数
            String relativePath = FileUtil.uploadProductImage(file, seller.getId(), productId, imageIndex);
            log.info("商品图片上传成功，返回路径: {}", relativePath);
            
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("商品图片上传失败: {}", e.getMessage(), e);
            
            // 尝试在异常处理时处理文件关闭
            try {
                file.getInputStream().close();
            } catch (Exception ex) {
                log.warn("关闭文件流失败，忽略此错误: {}", ex.getMessage());
            }
            
            return Result.error("商品图片上传失败: " + e.getMessage());
        } finally {
            // 尝试清理Tomcat临时文件
            try {
                file.getInputStream().close();
            } catch (Exception ex) {
                // 忽略关闭错误
            }
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
        log.info("获取卖家订单列表: status={}, page={}, size={}", status, page, size);
        try {
            // 获取当前卖家信息
            Seller seller = sellerService.getCurrentSeller();
            if (seller == null) {
                log.error("获取卖家信息失败");
                return Result.error("获取卖家信息失败，请重新登录");
            }
            
            log.info("卖家信息: id={}, shopName={}", seller.getId(), seller.getShopName());
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("sellerId", seller.getId());
            params.put("page", page);
            params.put("size", size);
            
            if (status != null && status >= 0) {
                log.info("按状态 {} 筛选订单", status);
                params.put("status", status.toString());
            }
            
            // 调用服务查询订单
            PageResult<OrderVO> result = orderService.getSellerOrders(params);
            log.info("查询结果: 总数={}, 订单数={}", result.getTotal(), result.getList().size());
            
            // 检查订单中的时间是否正确
            if (result.getList() != null && !result.getList().isEmpty()) {
                boolean hasNullTime = false;
                for (OrderVO order : result.getList()) {
                    if (order.getCreateTime() == null) {
                        hasNullTime = true;
                        log.warn("订单 {} 创建时间为null", order.getOrderNo());
                    }
                }
                if (hasNullTime) {
                    log.warn("部分订单创建时间为null，但已处理为当前时间");
                }
            }
            
            return Result.success(result);
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
        log.info("卖家发货请求: orderNo={}", orderNo);
        try {
            // 参数校验
            if (orderNo == null || orderNo.trim().isEmpty()) {
                log.error("订单号为空");
                return Result.error("订单号不能为空");
            }
            
            // 获取当前卖家信息
            Seller seller = sellerService.getCurrentSeller();
            if (seller == null) {
                log.error("获取卖家信息失败");
                return Result.error("获取卖家信息失败，请重新登录");
            }
            
            log.info("卖家 [{}] 正在为订单 [{}] 执行发货操作", seller.getId(), orderNo);
            
            // 执行发货操作
            boolean shipped = orderService.shipOrder(orderNo, seller.getId());
            
            if (shipped) {
                log.info("订单 [{}] 发货成功", orderNo);
                return Result.success(true);
            } else {
                log.warn("订单 [{}] 发货失败，可能状态不正确或已被更新", orderNo);
                return Result.error("发货失败，请刷新页面重试");
            }
        } catch (BusinessException e) {
            log.error("卖家发货业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("卖家发货系统异常", e);
            return Result.error("发货失败: " + e.getMessage());
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

    /**
     * 更新商家信息
     */
    @PutMapping("/info/update")
    public Result<Seller> updateSellerInfo(@RequestBody Map<String, Object> updateMap) {
        log.info("更新商家信息: {}", updateMap);
        try {
            Seller currentSeller = sellerService.getCurrentSeller();
            
            // 创建新的Seller对象
            Seller sellerInfo = new Seller();
            sellerInfo.setId(currentSeller.getId());
            sellerInfo.setUserId(currentSeller.getUserId());
            
            // 处理前端传来的字段
            if (updateMap.containsKey("shopName")) {
                sellerInfo.setShopName((String) updateMap.get("shopName"));
            }
            
            // 处理description/shopDesc字段
            if (updateMap.containsKey("description")) {
                sellerInfo.setShopDesc((String) updateMap.get("description"));
                log.info("从description字段获取描述: {}", updateMap.get("description"));
            } else if (updateMap.containsKey("shopDesc")) {
                sellerInfo.setShopDesc((String) updateMap.get("shopDesc"));
                log.info("从shopDesc字段获取描述: {}", updateMap.get("shopDesc"));
            }
            
            // 处理商家logo
            if (updateMap.containsKey("shopLogo")) {
                Object logoObj = updateMap.get("shopLogo");
                if (logoObj instanceof String) {
                    String logoStr = (String) logoObj;
                    if (logoStr.startsWith("{")) {
                        log.warn("商家logo格式不正确，可能是对象字符串: {}", logoStr);
                        sellerInfo.setShopLogo(currentSeller.getShopLogo());
                    } else {
                        // 处理重复的/api前缀
                        if (logoStr.startsWith("/api/api/")) {
                            logoStr = logoStr.replace("/api/api/", "/api/");
                            log.info("修正重复API前缀后的logo路径: {}", logoStr);
                        }
                        sellerInfo.setShopLogo(logoStr);
                    }
                } else {
                    log.warn("商家logo不是字符串类型");
                    sellerInfo.setShopLogo(currentSeller.getShopLogo());
                }
            }
            
            // 处理其他字段
            if (updateMap.containsKey("contactName")) {
                sellerInfo.setContactName((String) updateMap.get("contactName"));
            }
            if (updateMap.containsKey("contactPhone")) {
                sellerInfo.setContactPhone((String) updateMap.get("contactPhone"));
            }
            if (updateMap.containsKey("contactEmail")) {
                sellerInfo.setContactEmail((String) updateMap.get("contactEmail"));
            }
            
            log.info("处理后的商家信息: {}", sellerInfo);
            
            // 更新商家信息
            sellerService.updateSeller(sellerInfo);
            
            // 获取更新后的信息
            Seller updatedSeller = sellerService.getSellerInfo(currentSeller.getId());
            return Result.success(updatedSeller);
        } catch (Exception e) {
            log.error("更新商家信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除商品所有图片
     */
    @DeleteMapping("/images/product/all")
    public Result<Boolean> deleteAllProductImages(@RequestParam Long productId) {
        log.info("批量删除商品所有图片: productId={}", productId);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 检查商品是否属于当前卖家
            ProductVO existingProduct = productService.getProductDetail(productId);
            if (existingProduct == null) {
                return Result.error("商品不存在");
            }
            
            if (!existingProduct.getSellerId().equals(seller.getId())) {
                return Result.error("无权操作此商品");
            }
            
            // 删除图片
            boolean deleted = productService.deleteAllProductImages(productId);
            
            return Result.success(deleted);
        } catch (Exception e) {
            log.error("批量删除商品图片失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取销售数据分析
     */
    @GetMapping("/sales/analytics")
    public Result<Map<String, Object>> getSalesAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String period) {
        log.info("获取销售数据分析: startDate={}, endDate={}, period={}", startDate, endDate, period);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("sellerId", seller.getId());
            params.put("period", period);
            
            if (StringUtils.hasText(startDate)) {
                params.put("startDate", startDate);
            }
            
            if (StringUtils.hasText(endDate)) {
                params.put("endDate", endDate);
            }
            
            // 获取销售数据分析
            Map<String, Object> salesAnalytics = orderService.getSalesAnalytics(params);
            
            // 获取订单数据，增加这部分逻辑
            try {
                // 构建查询参数
                Map<String, Object> orderParams = new HashMap<>();
                orderParams.put("sellerId", seller.getId());
                
                // 使用相同的日期范围
                if (StringUtils.hasText(startDate)) {
                    orderParams.put("startDate", startDate);
                }
                
                if (StringUtils.hasText(endDate)) {
                    orderParams.put("endDate", endDate);
                }
                
                // 查询状态为已完成的订单
                orderParams.put("status", "3");
                orderParams.put("page", 1);
                orderParams.put("size", 10);
                
                // 调用服务查询订单
                PageResult<OrderVO> recentOrders = orderService.getSellerOrders(orderParams);
                log.info("查询到的订单: {} 条", recentOrders.getList().size());
                
                // 将订单数据添加到返回结果中
                salesAnalytics.put("recentOrders", recentOrders.getList());
            } catch (Exception e) {
                log.error("获取订单数据失败", e);
                salesAnalytics.put("recentOrders", new ArrayList<>());
            }
            
            return Result.success(salesAnalytics);
        } catch (Exception e) {
            log.error("获取销售数据分析失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 导出财务报表
     */
    @GetMapping("/financial/export")
    public void exportFinancialReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "monthly") String reportType,
            HttpServletResponse response) {
        log.info("导出财务报表: startDate={}, endDate={}, reportType={}", 
                startDate, endDate, reportType);
        try {
            Seller seller = sellerService.getCurrentSeller();
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("sellerId", seller.getId());
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("reportType", reportType);
            
            // 设置响应头
            String fileName = "financial_report_" + startDate + "_to_" + endDate;
            response.setContentType("text/csv;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".csv");
            
            // 导出报表
            orderService.exportFinancialReport(params, response.getOutputStream(), "csv");
            
        } catch (Exception e) {
            log.error("导出财务报表失败", e);
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":500,\"message\":\"" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
} 