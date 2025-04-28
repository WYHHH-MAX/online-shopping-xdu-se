package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.online.common.result.PageResult;
import com.shop.online.dto.AdminStatsDTO;
import com.shop.online.dto.OrderItemDTO;
import com.shop.online.dto.ProductDTO;
import com.shop.online.dto.SellerRequestDTO;
import com.shop.online.entity.Order;
import com.shop.online.entity.Product;
import com.shop.online.entity.Seller;
import com.shop.online.entity.User;
import com.shop.online.mapper.OrderItemMapper;
import com.shop.online.mapper.OrderMapper;
import com.shop.online.mapper.ProductMapper;
import com.shop.online.mapper.SellerMapper;
import com.shop.online.mapper.SellerRequestMapper;
import com.shop.online.mapper.UserMapper;
import com.shop.online.service.AdminService;
import com.shop.online.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SellerRequestMapper sellerRequestMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private SellerMapper sellerMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public AdminStatsDTO getAdminStats() {
        try {
            log.info("开始获取管理员统计数据");
            AdminStatsDTO statsDTO = new AdminStatsDTO();
            
            try {
                // 总用户数
                LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
                Long userCount = userMapper.selectCount(userQuery);
                statsDTO.setUserCount(userCount);
//                log.info("获取到总用户数: {}", userCount);
            } catch (Exception e) {
                log.error("获取总用户数时出错: {}", e.getMessage(), e);
                statsDTO.setUserCount(0L);
            }
            
            try {
                // 卖家数
                LambdaQueryWrapper<User> sellerQuery = new LambdaQueryWrapper<>();
                sellerQuery.eq(User::getRole, 1); // 1是卖家角色
                Long sellerCount = userMapper.selectCount(sellerQuery);
                statsDTO.setSellerCount(sellerCount);
//                log.info("获取到卖家数: {}", sellerCount);
            } catch (Exception e) {
                log.error("获取卖家数时出错: {}", e.getMessage(), e);
                statsDTO.setSellerCount(0L);
            }
            
            try {
                // 待处理申请数
                Long pendingCount = sellerRequestMapper.countPendingRequests();
                statsDTO.setPendingRequests(pendingCount);
//                log.info("获取到待处理申请数: {}", pendingCount);
            } catch (Exception e) {
                log.error("获取待处理申请数时出错: {}", e.getMessage(), e);
                statsDTO.setPendingRequests(0L);
            }
            
            try {
                // 商品总数
                Long productCount = productMapper.countProducts();
                statsDTO.setProductCount(productCount);
//                log.info("获取到商品总数: {}", productCount);
            } catch (Exception e) {
                log.error("获取商品总数时出错: {}", e.getMessage(), e);
                statsDTO.setProductCount(0L);
            }
            
            return statsDTO;
        } catch (Exception e) {
            log.error("获取管理员统计数据时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("获取管理员统计数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SellerRequestDTO> getPendingSellerRequests() {
        try {
//            log.info("开始获取待处理的卖家申请");
            // 查询待处理的申请
            LambdaQueryWrapper<Seller> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Seller::getStatus, 0) // 待审核状态
                        .orderByDesc(Seller::getCreatedTime);
            
            List<Seller> pendingRequests = sellerRequestMapper.selectList(queryWrapper);
//            log.info("获取到 {} 个待处理的卖家申请", pendingRequests.size());
            
            // 转换为DTO并返回
            List<SellerRequestDTO> dtoList = pendingRequests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
//            log.info("转换后的DTO列表大小: {}", dtoList.size());
            return dtoList;
        } catch (Exception e) {
            log.error("获取待处理的卖家申请时出错: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public PageResult<SellerRequestDTO> getSellerRequests(Integer status, int page, int pageSize) {
        try {
//            log.info("开始获取卖家申请列表, 状态: {}, 页码: {}, 每页数量: {}", status, page, pageSize);
            // 构建查询条件
            LambdaQueryWrapper<Seller> queryWrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                queryWrapper.eq(Seller::getStatus, status);
                log.info("根据状态 {} 筛选卖家申请", status);
            }
            queryWrapper.orderByDesc(Seller::getCreatedTime);
            
            // 打印SQL日志用于调试
//            log.info("执行查询卖家申请, 条件: status={}", status);
            
            // 分页查询
            Page<Seller> pageParam = new Page<>(page, pageSize);
            Page<Seller> pageResult = sellerRequestMapper.selectPage(pageParam, queryWrapper);
//            log.info("分页查询结果: 总记录数: {}, 当前页记录数: {}", pageResult.getTotal(), pageResult.getRecords().size());
            
            // 转换为DTO
            List<SellerRequestDTO> dtoList = pageResult.getRecords().stream()
                                                      .map(this::convertToDTO)
                                                      .collect(Collectors.toList());
            
//            log.info("转换后的DTO列表: {}", dtoList);
            
            return new PageResult<>(pageResult.getTotal(), dtoList);
        } catch (Exception e) {
            log.error("获取卖家申请列表时出错: {}", e.getMessage(), e);
            return new PageResult<>(0L, new ArrayList<>());
        }
    }

    @Override
    @Transactional
    public boolean approveSellerRequest(Long id, Long adminId) {
        try {
//            log.info("开始处理通过卖家申请, 申请ID: {}, 管理员ID: {}", id, adminId);
            // 获取申请
            Seller seller = sellerRequestMapper.selectById(id);
            if (seller == null) {
                log.error("找不到ID为 {} 的卖家申请", id);
                throw new RuntimeException("申请不存在");
            }
            
            if (seller.getStatus() != 0) {
                log.error("ID为 {} 的申请已被处理, 当前状态: {}", id, seller.getStatus());
                throw new RuntimeException("该申请已被处理");
            }
            
            // 更新申请状态
            seller.setStatus(1); // 设置为已通过
            seller.setUpdatedTime(LocalDateTime.now());
            // seller没有reviewerId字段，暂时忽略adminId
            int rows = sellerRequestMapper.updateById(seller);
            log.info("更新卖家申请状态结果: {}", rows > 0 ? "成功" : "失败");
            
            // 更新用户角色为卖家
            User user = userMapper.selectById(seller.getUserId());
            if (user != null) {
                user.setRole(1); // 设置为卖家角色
                int userRows = userMapper.updateById(user);
//                log.info("更新用户角色结果: {}", userRows > 0 ? "成功" : "失败");
            } else {
                log.warn("找不到ID为 {} 的用户", seller.getUserId());
            }
            
            return true;
        } catch (RuntimeException e) {
            log.error("通过卖家申请时出错: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("通过卖家申请时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("通过卖家申请失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean rejectSellerRequest(Long id, Long adminId, String reason) {
        try {
            log.info("开始处理拒绝卖家申请, 申请ID: {}, 管理员ID: {}, 拒绝理由: {}", id, adminId, reason);
            // 获取申请
            Seller seller = sellerRequestMapper.selectById(id);
            if (seller == null) {
                log.error("找不到ID为 {} 的卖家申请", id);
                throw new RuntimeException("申请不存在");
            }
            
            if (seller.getStatus() != 0) {
                log.error("ID为 {} 的申请已被处理, 当前状态: {}", id, seller.getStatus());
                throw new RuntimeException("该申请已被处理");
            }
            
            // 更新申请状态
            seller.setStatus(2); // 设置为已拒绝
            seller.setUpdatedTime(LocalDateTime.now());
            seller.setRejectReason(reason);
            int rows = sellerRequestMapper.updateById(seller);
            log.info("更新卖家申请状态结果: {}", rows > 0 ? "成功" : "失败");
            
            return true;
        } catch (RuntimeException e) {
            log.error("拒绝卖家申请时出错: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("拒绝卖家申请时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("拒绝卖家申请失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PageResult<User> getUsers(String username, Integer role, Integer status, int page, int pageSize) {
        try {
            log.info("开始获取用户列表, 用户名: {}, 角色: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                     username, role, status, page, pageSize);
            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            if (username != null && !username.isEmpty()) {
                queryWrapper.like(User::getUsername, username);
            }
            
            if (role != null) {
                queryWrapper.eq(User::getRole, role);
            }
            
            if (status != null) {
                queryWrapper.eq(User::getStatus, status);
            }
            
            // 分页查询
            Page<User> pageParam = new Page<>(page, pageSize);
            Page<User> pageResult = userMapper.selectPage(pageParam, queryWrapper);
            log.info("分页查询结果: 总记录数: {}, 当前页记录数: {}", 
                     pageResult.getTotal(), pageResult.getRecords().size());
            
            return new PageResult<>(pageResult.getTotal(), pageResult.getRecords());
        } catch (Exception e) {
            log.error("获取用户列表时出错: {}", e.getMessage(), e);
            return new PageResult<>(0L, new ArrayList<>());
        }
    }

    @Override
    @Transactional
    public boolean updateUserStatus(Long id, Integer status) {
        try {
            log.info("开始更新用户状态, 用户ID: {}, 新状态: {}", id, status);
            User user = userMapper.selectById(id);
            if (user == null) {
                log.error("找不到ID为 {} 的用户", id);
                throw new RuntimeException("用户不存在");
            }
            
            user.setStatus(status);
            int rows = userMapper.updateById(user);
            log.info("更新用户状态结果: {}", rows > 0 ? "成功" : "失败");
            
            return true;
        } catch (RuntimeException e) {
            log.error("更新用户状态时出错: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新用户状态时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("更新用户状态失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PageResult<ProductDTO> getProducts(String name, Integer status, int page, int pageSize) {
        try {
            log.info("开始获取商品列表, 商品名称: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                    name, status, page, pageSize);
            
            // 构建查询条件
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            
            if (name != null && !name.isEmpty()) {
                queryWrapper.like(Product::getName, name);
                log.info("根据商品名称 {} 模糊查询", name);
            }
            
            if (status != null) {
                queryWrapper.eq(Product::getStatus, status);
                log.info("根据状态 {} 筛选商品", status);
            }
            
            queryWrapper.orderByDesc(Product::getCreatedTime);
            
            // 分页查询
            Page<Product> pageParam = new Page<>(page, pageSize);
            Page<Product> pageResult = productMapper.selectPage(pageParam, queryWrapper);
            log.info("分页查询结果: 总记录数: {}, 当前页记录数: {}", 
                    pageResult.getTotal(), pageResult.getRecords().size());
            
            // 转换为DTO
            List<ProductDTO> dtoList = pageResult.getRecords().stream()
                    .map(this::convertToProductDTO)
                    .collect(Collectors.toList());
            
            return new PageResult<>(pageResult.getTotal(), dtoList);
        } catch (Exception e) {
            log.error("获取商品列表时出错: {}", e.getMessage(), e);
            return new PageResult<>(0L, new ArrayList<>());
        }
    }

    @Override
    public ProductDTO getProductById(Long id) {
        try {
            log.info("开始获取商品详情, 商品ID: {}", id);
            Product product = productMapper.selectById(id);
            if (product == null) {
                log.error("找不到ID为 {} 的商品", id);
                throw new RuntimeException("商品不存在");
            }
            
            ProductDTO productDTO = convertToProductDTO(product);
            log.info("获取商品详情成功, 商品名称: {}", productDTO.getName());
            
            return productDTO;
        } catch (RuntimeException e) {
            log.error("获取商品详情时出错: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取商品详情时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取商品详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean updateProductStatus(Long id, Integer status) {
        try {
            log.info("开始更新商品状态, 商品ID: {}, 新状态: {}", id, status);
            Product product = productMapper.selectById(id);
            if (product == null) {
                log.error("找不到ID为 {} 的商品", id);
                throw new RuntimeException("商品不存在");
            }
            
            product.setStatus(status);
            product.setUpdatedTime(LocalDateTime.now());
            int rows = productMapper.updateById(product);
            log.info("更新商品状态结果: {}", rows > 0 ? "成功" : "失败");
            
            return rows > 0;
        } catch (RuntimeException e) {
            log.error("更新商品状态时出错: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新商品状态时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("更新商品状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        try {
            log.info("开始删除商品, 商品ID: {}", id);
            Product product = productMapper.selectById(id);
            if (product == null) {
                log.error("找不到ID为 {} 的商品", id);
                throw new RuntimeException("商品不存在");
            }
            
            int rows = productMapper.deleteById(id);
            log.info("删除商品结果: {}", rows > 0 ? "成功" : "失败");
            
            return rows > 0;
        } catch (RuntimeException e) {
            log.error("删除商品时出错: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除商品时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("删除商品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PageResult<Seller> getAllSellers(String shopName, Integer status, int page, int pageSize) {
        log.info("获取所有卖家列表, 店铺名称: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                shopName, status, page, pageSize);
        try {
            // 构建查询条件
            LambdaQueryWrapper<Seller> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加店铺名称过滤
            if (StringUtils.hasText(shopName)) {
                queryWrapper.like(Seller::getShopName, shopName);
                log.info("按店铺名称过滤: {}", shopName);
            }
            
            // 添加状态过滤
            if (status != null) {
                queryWrapper.eq(Seller::getStatus, status);
                log.info("按状态过滤: {}", status);
            }
            
            // 按创建时间倒序排序
            queryWrapper.orderByDesc(Seller::getCreatedTime);
            
            // 分页查询
            Page<Seller> sellerPage = new Page<>(page, pageSize);
            Page<Seller> resultPage = sellerMapper.selectPage(sellerPage, queryWrapper);
            
            // 封装结果
            List<Seller> sellers = resultPage.getRecords();
            long total = resultPage.getTotal();
            
            log.info("查询到卖家数量: {}, 总记录数: {}", sellers.size(), total);
            
            return new PageResult<Seller>(total, sellers);
        } catch (Exception e) {
            log.error("获取卖家列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取卖家列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public boolean updateSellerStatus(Long id, Integer status) {
        log.info("更新卖家状态, ID: {}, 新状态: {}", id, status);
        
        try {
            if (id == null || status == null) {
                log.error("参数错误: ID或状态为空");
                throw new IllegalArgumentException("参数错误: ID或状态为空");
            }
            
            // 验证状态值
            if (status < 0 || status > 2) {
                log.error("状态值无效: {}", status);
                throw new IllegalArgumentException("状态值无效，必须是0-2之间的值");
            }
            
            // 查询卖家是否存在
            Seller seller = sellerMapper.selectById(id);
            if (seller == null) {
                log.error("卖家不存在, ID: {}", id);
                throw new RuntimeException("卖家不存在");
            }
            
            // 更新状态
            Seller updateSeller = new Seller();
            updateSeller.setId(id);
            updateSeller.setStatus(status);
            updateSeller.setUpdatedTime(LocalDateTime.now());
            
            int result = sellerMapper.updateById(updateSeller);
            log.info("更新卖家状态结果: {}", result > 0);
            
            // 如果是将状态改为已通过(1)，需要更新用户角色为卖家
            if (status == 1) {
                Long userId = seller.getUserId();
                if (userId != null) {
                    log.info("更新用户角色为卖家, 用户ID: {}", userId);
                    User user = userMapper.selectById(userId);
                    if (user != null) {
                        User updateUser = new User();
                        updateUser.setId(userId);
                        updateUser.setRole(1); // 1表示卖家角色
                        userMapper.updateById(updateUser);
                        log.info("用户角色已更新为卖家, 用户ID: {}", userId);
                    } else {
                        log.warn("未找到对应的用户, 用户ID: {}", userId);
                    }
                }
            }
            
            return result > 0;
        } catch (Exception e) {
            log.error("更新卖家状态失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新卖家状态失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public boolean deleteSeller(Long id) {
        log.info("删除卖家, ID: {}", id);
        
        try {
            if (id == null) {
                log.error("参数错误: ID为空");
                throw new IllegalArgumentException("参数错误: ID为空");
            }
            
            // 查询卖家是否存在
            Seller seller = sellerMapper.selectById(id);
            if (seller == null) {
                log.error("卖家不存在, ID: {}", id);
                throw new RuntimeException("卖家不存在");
            }
            
            // 删除卖家记录
            int result = sellerMapper.deleteById(id);
            log.info("删除卖家结果: {}", result > 0);
            
            return result > 0;
        } catch (Exception e) {
            log.error("删除卖家失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除卖家失败: " + e.getMessage(), e);
        }
    }
    
    // 将Seller转换为DTO，包含用户信息
    private SellerRequestDTO convertToDTO(Seller seller) {
        try {
            SellerRequestDTO dto = new SellerRequestDTO();
            dto.setId(seller.getId());
            dto.setUserId(seller.getUserId());
            dto.setReason(seller.getShopDesc()); // 使用商店描述作为申请理由
            dto.setStatus(seller.getStatus());
            
            // 格式化时间
            if (seller.getCreatedTime() != null) {
                dto.setApplyTime(seller.getCreatedTime().format(FORMATTER));
            }
            
            if (seller.getUpdatedTime() != null) {
                dto.setReviewTime(seller.getUpdatedTime().format(FORMATTER));
            }
            
            // 暂时不设置审核者ID
            dto.setReviewerId(null);
            dto.setReviewReason(seller.getRejectReason());
            
            // 获取用户信息
            User user = userMapper.selectById(seller.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setPhone(seller.getContactPhone()); // 使用商家联系电话
                dto.setEmail(seller.getContactEmail()); // 使用商家联系邮箱
                dto.setAvatar(user.getAvatar());
            } else {
                log.warn("找不到ID为 {} 的用户", seller.getUserId());
            }
            
            return dto;
        } catch (Exception e) {
            log.error("转换Seller到DTO时出错: {}", e.getMessage(), e);
            // 返回基本信息，避免整个列表因一条记录转换失败而失败
            SellerRequestDTO dto = new SellerRequestDTO();
            dto.setId(seller.getId());
            dto.setUserId(seller.getUserId());
            dto.setStatus(seller.getStatus());
            return dto;
        }
    }
    
    // 将Product转换为ProductDTO
    private ProductDTO convertToProductDTO(Product product) {
        try {
            ProductDTO dto = new ProductDTO();
            // 复制基本属性
            BeanUtils.copyProperties(product, dto);
            
            // 处理特殊字段
            if (product.getCreatedTime() != null) {
                dto.setCreatedTime(product.getCreatedTime().format(FORMATTER));
            }
            
            if (product.getUpdatedTime() != null) {
                dto.setUpdatedTime(product.getUpdatedTime().format(FORMATTER));
            }
            
            // 获取卖家信息
            if (product.getSellerId() != null) {
                dto.setSellerId(product.getSellerId());
                
                // 尝试从seller表获取店铺名称
                Seller seller = sellerRequestMapper.selectById(product.getSellerId());
                if (seller != null) {
                    dto.setSellerName(seller.getShopName());
                } else {
                    log.warn("找不到ID为 {} 的卖家", product.getSellerId());
                    dto.setSellerName("未知卖家");
                }
            }
            
            // 从销售订单中获取销售数量（这里暂时设置为0，实际应该从订单系统获取）
            dto.setSalesCount(0);
            
            return dto;
        } catch (Exception e) {
            log.error("转换Product到DTO时出错: {}", e.getMessage(), e);
            // 返回基本信息
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setStatus(product.getStatus());
            return dto;
        }
    }

    @Override
    @Transactional
    public boolean setProductFeatured(Long id, Integer featured) {
        log.info("设置商品推荐状态, 商品ID: {}, 推荐状态: {}", id, featured);
        
        try {
            if (id == null || featured == null) {
                log.error("参数错误: ID或推荐状态为空");
                throw new IllegalArgumentException("参数错误: ID或推荐状态为空");
            }
            
            // 验证状态值
            if (featured != 0 && featured != 1) {
                log.error("推荐状态值无效: {}", featured);
                throw new IllegalArgumentException("推荐状态值无效，必须是0或1");
            }
            
            // 查询商品是否存在
            Product product = productMapper.selectById(id);
            if (product == null) {
                log.error("商品不存在, ID: {}", id);
                throw new RuntimeException("商品不存在");
            }
            
            // 更新推荐状态
            Product updateProduct = new Product();
            updateProduct.setId(id);
            updateProduct.setIsFeatured(featured);
            updateProduct.setUpdatedTime(LocalDateTime.now());
            
            int result = productMapper.updateById(updateProduct);
            log.info("更新商品推荐状态结果: {}", result > 0);
            
            return result > 0;
        } catch (Exception e) {
            log.error("设置商品推荐状态失败: {}", e.getMessage(), e);
            throw new RuntimeException("设置商品推荐状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PageResult<?> getAllOrders(String orderNo, String username, Integer status, int page, int pageSize) {
        log.info("获取所有订单列表, 订单号: {}, 用户名: {}, 状态: {}, 页码: {}, 每页数量: {}", 
                orderNo, username, status, page, pageSize);
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            
            // 订单号查询
            if (StringUtils.hasText(orderNo)) {
                queryWrapper.like(Order::getOrderNo, orderNo);
            }
            
            // 订单状态查询
            if (status != null) {
                queryWrapper.eq(Order::getStatus, status);
            }
            
            // 用户名查询（需要先根据用户名查找用户ID）
            if (StringUtils.hasText(username)) {
                LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
                userQuery.like(User::getUsername, username);
                List<User> users = userMapper.selectList(userQuery);
                
                if (!users.isEmpty()) {
                    List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
                    queryWrapper.in(Order::getUserId, userIds);
                } else {
                    // 如果找不到对应用户，返回空结果
                    return PageResult.of(0L, new ArrayList<>());
                }
            }
            
            // 只查询未删除的订单
            queryWrapper.eq(Order::getDeleted, 0);
            
            // 按创建时间倒序排列
            queryWrapper.orderByDesc(Order::getCreatedTime);
            
            // 执行分页查询
            Page<Order> pageResult = new Page<>(page, pageSize);
            Page<Order> orderPage = orderMapper.selectPage(pageResult, queryWrapper);
            
            // 转换为前端需要的格式
            List<OrderVO> orderVOList = new ArrayList<>();
            
            if (orderPage.getRecords() != null && !orderPage.getRecords().isEmpty()) {
                // 查询订单项信息
                List<Long> orderIds = orderPage.getRecords().stream()
                        .map(Order::getId)
                        .collect(Collectors.toList());
                
                // 查询订单对应的订单项
                List<OrderItemDTO> orderItems = orderItemMapper.selectByOrderIds(orderIds);
                
                // 按订单ID分组
                Map<Long, List<OrderItemDTO>> orderItemMap = orderItems.stream()
                        .collect(Collectors.groupingBy(OrderItemDTO::getOrderId));
                
                // 查询用户信息，用于返回用户名
                Map<Long, String> userMap = new HashMap<>();
                List<Long> userIds = orderPage.getRecords().stream()
                        .map(Order::getUserId)
                        .collect(Collectors.toList());
                
                if (!userIds.isEmpty()) {
                    LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
                    userQuery.in(User::getId, userIds);
                    List<User> users = userMapper.selectList(userQuery);
                    
                    for (User user : users) {
                        userMap.put(user.getId(), user.getUsername());
                    }
                }
                
                // 转换为VO
                for (Order order : orderPage.getRecords()) {
                    OrderVO orderVO = new OrderVO();
                    orderVO.setId(order.getId().intValue());
                    orderVO.setOrderNo(order.getOrderNo());
                    orderVO.setStatus(order.getStatus().toString());
                    orderVO.setTotalAmount(order.getTotalAmount());
                    orderVO.setCreateTime(order.getCreatedTime());
                    orderVO.setUpdateTime(order.getUpdatedTime());
                    orderVO.setUserId(order.getUserId());
                    orderVO.setUsername(userMap.getOrDefault(order.getUserId(), "未知用户"));
                    
                    // 设置支付方式
                    if (order.getPaymentMethod() != null) {
                        orderVO.setPaymentMethod(order.getPaymentMethod().toString());
                    }
                    
                    // 设置订单商品
                    List<OrderItemDTO> items = orderItemMap.getOrDefault(order.getId(), new ArrayList<>());
                    List<OrderVO.OrderProductVO> productVOList = items.stream().map(item -> {
                        OrderVO.OrderProductVO productVO = new OrderVO.OrderProductVO();
                        productVO.setId(item.getProductId().intValue());
                        productVO.setName(item.getProductName());
                        productVO.setImage(item.getProductImage());
                        productVO.setPrice(item.getPrice());
                        productVO.setQuantity(item.getQuantity());
                        return productVO;
                    }).collect(Collectors.toList());
                    
                    orderVO.setProducts(productVOList);
                    
                    orderVOList.add(orderVO);
                }
            }
            
            return PageResult.of(orderPage.getTotal(), orderVOList);
        } catch (Exception e) {
            log.error("获取所有订单列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取所有订单列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public boolean shipOrder(String orderNo) {
        log.info("管理员发货操作, 订单号: {}", orderNo);
        try {
            // 查询订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderNo, orderNo)
                      .eq(Order::getDeleted, 0); // 未删除的订单
            
            Order order = orderMapper.selectOne(queryWrapper);
            
            if (order == null) {
                log.error("订单不存在, 订单号: {}", orderNo);
                throw new RuntimeException("订单不存在");
            }
            
            // 检查订单状态是否为待发货
            if (order.getStatus() != 1) {
                log.error("订单状态不是待发货, 订单号: {}, 状态: {}", orderNo, order.getStatus());
                throw new RuntimeException("订单状态不是待发货");
            }
            
            // 更新订单状态为待收货
            order.setStatus(2); // 待收货
            order.setUpdatedTime(LocalDateTime.now());
            
            int rows = orderMapper.updateById(order);
            
            log.info("管理员发货成功, 订单号: {}", orderNo);
            
            return rows > 0;
        } catch (Exception e) {
            log.error("管理员发货失败, 订单号: {}, 错误: {}", orderNo, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取订单详情
     */
    @Override
    public OrderVO getOrderDetail(String orderNo) {
        log.info("管理员获取订单详情, 订单号: {}", orderNo);
        try {
            // 查询订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderNo, orderNo);
            queryWrapper.eq(Order::getDeleted, 0); // 只查询未删除订单
            Order order = orderMapper.selectOne(queryWrapper);
            
            if (order == null) {
                log.error("订单不存在, 订单号: {}", orderNo);
                throw new RuntimeException("订单不存在");
            }
            
            // 查询订单项
            List<OrderItemDTO> orderItems = orderItemMapper.selectByOrderId(order.getId());
            
            // 查询用户信息
            User user = userMapper.selectById(order.getUserId());
            
            // 转换VO
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setStatus(order.getStatus().toString());
            orderVO.setCreateTime(order.getCreatedTime());
            orderVO.setUpdateTime(order.getUpdatedTime());
            
            // 设置用户信息
            if (user != null) {
                orderVO.setUsername(user.getUsername());
            } else {
                orderVO.setUsername("未知用户");
            }
            
            // 设置支付方式（根据订单的paymentMethod字段）
            if (order.getPaymentMethod() != null) {
                orderVO.setPaymentMethod(order.getPaymentMethod().toString());
            } else {
                orderVO.setPaymentMethod("未知");
            }
            
            // 转换订单商品信息
            List<OrderVO.OrderProductVO> productVOList = orderItems.stream().map(item -> {
                OrderVO.OrderProductVO productVO = new OrderVO.OrderProductVO();
                productVO.setId(item.getProductId().intValue());
                productVO.setName(item.getProductName());
                productVO.setImage(item.getProductImage());
                productVO.setPrice(item.getPrice());
                productVO.setQuantity(item.getQuantity());
                return productVO;
            }).collect(Collectors.toList());
            
            orderVO.setProducts(productVOList);
            
            log.info("管理员获取订单详情成功, 订单号: {}", orderNo);
            
            return orderVO;
        } catch (Exception e) {
            log.error("管理员获取订单详情失败, 订单号: {}, 错误: {}", orderNo, e.getMessage(), e);
            throw e;
        }
    }
} 