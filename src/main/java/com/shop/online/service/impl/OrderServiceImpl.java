package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.common.result.PageResult;
import com.shop.online.dto.CreateOrderDTO;
import com.shop.online.dto.OrderItemDTO;
import com.shop.online.dto.OrderQueryDTO;
import com.shop.online.entity.Cart;
import com.shop.online.entity.Order;
import com.shop.online.entity.Product;
import com.shop.online.exception.BusinessException;
import com.shop.online.mapper.CartMapper;
import com.shop.online.mapper.OrderItemMapper;
import com.shop.online.mapper.OrderMapper;
import com.shop.online.mapper.ProductMapper;
import com.shop.online.service.OrderService;
import com.shop.online.service.UserService;
import com.shop.online.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserService userService;

    /**
     * 获取订单列表
     */
    @Override
    public PageResult<OrderVO> getOrders(OrderQueryDTO queryDTO) {
        // 打印请求参数
        log.info("获取订单列表，查询参数: {}", queryDTO);
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            
            // 获取当前用户ID
//            log.info("尝试获取当前用户...");
            com.shop.online.entity.User currentUser = userService.getCurrentUser();
            
            if (currentUser == null) {
                log.error("获取当前用户失败，返回空结果");
                throw new BusinessException("获取当前用户信息失败，请重新登录");
            }
            
            Long userId = currentUser.getId();
//            log.info("当前用户ID: {}, 用户名: {}", userId, currentUser.getUsername());
            
            queryWrapper.eq(Order::getUserId, userId);
            
            // 根据状态筛选
            if (StringUtils.hasText(queryDTO.getStatus())) {
//                log.info("按状态筛选订单: {}", queryDTO.getStatus());
                queryWrapper.eq(Order::getStatus, queryDTO.getStatus());
            }
            
            // 只查询未删除的订单
            queryWrapper.eq(Order::getDeleted, 0);
            
            // 按创建时间倒序
            queryWrapper.orderByDesc(Order::getCreatedTime);
            
            // 分页查询
            Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
//            log.info("执行订单分页查询: 页码={}, 每页数量={}", queryDTO.getPage(), queryDTO.getSize());
            Page<Order> orderPage = baseMapper.selectPage(page, queryWrapper);
            
//            log.info("查询结果: 总条数={}, 记录数量={}", orderPage.getTotal(), orderPage.getRecords().size());
            
            if (CollectionUtils.isEmpty(orderPage.getRecords())) {
                log.info("查询结果为空，返回空列表");
                // 确保返回正确的格式
                PageResult<OrderVO> emptyResult = PageResult.of(orderPage.getTotal(), new ArrayList<>());
//                log.info("返回空结果: {}", emptyResult);
                return emptyResult;
            }
            
            // 查询订单项
            List<Long> orderIds = orderPage.getRecords().stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());
            
            // 查询订单对应的订单项
            List<OrderItemDTO> orderItems = orderItemMapper.selectByOrderIds(orderIds);
//            log.info("查询到订单项数量: {}", orderItems.size());
            
            // 按订单ID分组
            Map<Long, List<OrderItemDTO>> orderItemMap = orderItems.stream()
                    .collect(Collectors.groupingBy(OrderItemDTO::getOrderId));
            
            // 转换VO
            List<OrderVO> orderVOList = orderPage.getRecords().stream().map(order -> {
                OrderVO orderVO = new OrderVO();
                
                // 不要使用BeanUtils复制，因为字段名不一致，直接设置
                orderVO.setId(order.getId().intValue());
                orderVO.setOrderNo(order.getOrderNo());
                orderVO.setStatus(order.getStatus().toString());
                orderVO.setTotalAmount(order.getTotalAmount());
                
                // 显式设置创建时间和更新时间
                if (order.getCreatedTime() != null) {
                    orderVO.setCreateTime(order.getCreatedTime());
//                    log.info("订单创建、时间设置: {} -> {}", order.getOrderNo(), order.getCreatedTime());
                } else {
                    LocalDateTime now = LocalDateTime.now();
                    orderVO.setCreateTime(now);
                    log.warn("订单 {} 创建时间为null，设置为当前时间: {}", order.getOrderNo(), now);
                }
                
                if (order.getUpdatedTime() != null) {
                    orderVO.setUpdateTime(order.getUpdatedTime());
                } else {
                    orderVO.setUpdateTime(LocalDateTime.now());
                }
                
                // 封装订单商品
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
                
                return orderVO;
            }).collect(Collectors.toList());
            
            PageResult<OrderVO> result = PageResult.of(orderPage.getTotal(), orderVOList);
//            log.info("返回订单列表结果: 总数={}, 条目数={}", result.getTotal(), result.getList().size());
            return result;
        } catch (Exception e) {
            log.error("获取订单列表时发生异常", e);
            throw e;
        }
    }

    /**
     * 创建订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO createOrderDTO) {
//        log.info("开始创建订单: {}", createOrderDTO);
        
        try {
            // 获取当前用户
            Long userId = userService.getCurrentUser().getId();
//            log.info("当前用户ID: {}", userId);
            
            // 判断是否是直接购买
            boolean isDirectBuy = createOrderDTO.getDirectBuy() != null && createOrderDTO.getDirectBuy();
//            log.info("订单类型: {}", isDirectBuy ? "直接购买" : "购物车结算");
            
            List<Cart> cartItems;
            Map<Long, Product> productMap;
            BigDecimal totalAmount = BigDecimal.ZERO;
            Long sellerId = null;
            
            if (isDirectBuy) {
                // 直接购买模式
                if (createOrderDTO.getProductId() == null || createOrderDTO.getQuantity() == null) {
                    log.error("直接购买参数不完整: productId={}, quantity={}", 
                            createOrderDTO.getProductId(), createOrderDTO.getQuantity());
                    throw new BusinessException("直接购买参数不完整");
                }
                
                // 查询商品信息
                Long productId = createOrderDTO.getProductId().longValue();
                Integer quantity = createOrderDTO.getQuantity();
                
//                log.info("直接购买商品, 商品ID: {}, 数量: {}", productId, quantity);
                
                Product product = productMapper.selectById(productId);
                if (product == null) {
                    log.error("商品不存在, 商品ID: {}", productId);
                    throw new BusinessException("商品不存在");
                }
                
                // 检查库存
                if (product.getStock() < quantity) {
                    log.error("商品库存不足, 商品: {}, 库存: {}, 需要: {}", 
                            product.getName(), product.getStock(), quantity);
                    throw new BusinessException(product.getName() + "库存不足");
                }
                
                // 计算总金额
                totalAmount = product.getPrice().multiply(new BigDecimal(quantity));
//                log.info("商品总金额: {}", totalAmount);
                
                // 获取卖家ID
                sellerId = product.getSellerId();
//                log.info("订单卖家ID: {}", sellerId);
                
                // 创建临时购物车项用于后续处理
                Cart tempCart = new Cart();
                tempCart.setProductId(productId);
                tempCart.setQuantity(quantity);
                tempCart.setUserId(userId);
                tempCart.setSelected(1); // 选中状态
                tempCart.setCreatedTime(LocalDateTime.now());
                tempCart.setUpdatedTime(LocalDateTime.now());
                tempCart.setDeleted(0);
                
                cartItems = Collections.singletonList(tempCart);
                productMap = Collections.singletonMap(productId, product);
            } else {
                // 购物车模式
                // 验证参数
                List<Integer> cartItemIds = createOrderDTO.getCartItemIds();
                if (CollectionUtils.isEmpty(cartItemIds)) {
                    log.error("购物车项为空");
                    throw new BusinessException("请选择要购买的商品");
                }
                
                // 查询购物车项
                LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(Cart::getId, cartItemIds.stream().map(Integer::longValue).collect(Collectors.toList()));
                queryWrapper.eq(Cart::getUserId, userId);
                queryWrapper.eq(Cart::getDeleted, 0); // 只查询未删除的购物车项
                cartItems = cartMapper.selectList(queryWrapper);
//                log.info("查询到的购物车项: {}", cartItems);
                
                if (CollectionUtils.isEmpty(cartItems) || cartItems.size() != cartItemIds.size()) {
                    log.error("部分商品不在购物车中, 预期数量: {}, 实际数量: {}", 
                            cartItemIds.size(), cartItems != null ? cartItems.size() : 0);
                    throw new BusinessException("部分商品不在购物车中");
                }
                
                // 查询商品信息
                List<Long> productIds = cartItems.stream()
                        .map(Cart::getProductId)
                        .collect(Collectors.toList());
//                log.info("查询商品信息, 商品IDs: {}", productIds);
                
                List<Product> products = productMapper.selectBatchIds(productIds);
//                log.info("查询到的商品: {}", products);
                
                productMap = products.stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));
                
                // 计算总金额和确保有卖家信息
                for (Cart cartItem : cartItems) {
                    Product product = productMap.get(cartItem.getProductId());
                    if (product == null) {
                        log.error("商品不存在, 商品ID: {}", cartItem.getProductId());
                        throw new BusinessException("商品不存在");
                    }
                    
                    if (product.getStock() < cartItem.getQuantity()) {
                        log.error("商品库存不足, 商品: {}, 库存: {}, 需要: {}", 
                                product.getName(), product.getStock(), cartItem.getQuantity());
                        throw new BusinessException(product.getName() + "库存不足");
                    }
                    
                    // 累加金额
                    BigDecimal itemAmount = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
                    totalAmount = totalAmount.add(itemAmount);
                    log.debug("商品: {}, 单价: {}, 数量: {}, 小计: {}", 
                            product.getName(), product.getPrice(), cartItem.getQuantity(), itemAmount);
                    
                    // 获取卖家ID（简化处理，实际可能需要按卖家拆分订单）
                    if (sellerId == null) {
                        sellerId = product.getSellerId();
//                        log.info("订单卖家ID: {}", sellerId);
                    }
                }
            }
            
            if (sellerId == null) {
                log.error("无法确定卖家信息");
                throw new BusinessException("无法确定卖家信息");
            }
            
            // 生成订单号
            String orderNo = generateOrderNo();
//            log.info("生成订单号: {}", orderNo);
            
            // 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setSellerId(sellerId);
            order.setStatus(0); // 待付款
            order.setTotalAmount(totalAmount);
            order.setCreatedTime(LocalDateTime.now());
            order.setUpdatedTime(LocalDateTime.now());
            order.setDeleted(0); // 未删除
            
//            log.info("保存订单: {}", order);
            baseMapper.insert(order);
//            log.info("订单已保存, ID: {}", order.getId());
            
            // 创建订单项
            List<OrderItemDTO> orderItems = new ArrayList<>();
            for (Cart cartItem : cartItems) {
                Product product = productMap.get(cartItem.getProductId());
                
                OrderItemDTO orderItem = OrderItemDTO.fromCart(cartItem, product);
                orderItem.setOrderId(order.getId());
                
                orderItems.add(orderItem);
                
                // 扣减库存
                product.setStock(product.getStock() - cartItem.getQuantity());
                productMapper.updateById(product);
//                log.debug("商品库存已更新, 商品: {}, 新库存: {}", product.getName(), product.getStock());
            }
            
//            log.info("保存订单项, 数量: {}", orderItems.size());
            orderItemMapper.insertBatch(orderItems);
            
            // 如果是购物车模式，需要删除购物车项
            if (!isDirectBuy && createOrderDTO.getCartItemIds() != null) {
                List<Long> longCartItemIds = createOrderDTO.getCartItemIds().stream()
                        .map(Integer::longValue)
                        .collect(Collectors.toList());
//                log.info("删除购物车项, IDs: {}", longCartItemIds);
                cartMapper.deleteBatchIds(longCartItemIds);
            }
            
            // 返回订单信息
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setStatus(order.getStatus().toString());
            
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
            
//            log.info("订单创建成功: {}", orderVO);
            return orderVO;
        } catch (Exception e) {
            log.error("创建订单失败", e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }

    /**
     * 支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo) {
//        log.info("开始处理支付订单请求，订单号: {}", orderNo);
        
        if (orderNo == null || orderNo.trim().isEmpty()) {
            log.error("订单号为空，无法支付");
            throw new BusinessException("订单号不能为空");
        }
        
        try {
            // 清理订单号（防止前端传入错误格式）
            String cleanOrderNo = orderNo.trim();
            // 去除可能的URL部分
            if (cleanOrderNo.contains("?") || cleanOrderNo.contains("/")) {
                log.warn("订单号包含特殊字符，尝试清理: {}", cleanOrderNo);
                String[] parts = cleanOrderNo.split("[?/]");
                cleanOrderNo = parts[parts.length - 1];
//                log.info("清理后的订单号: {}", cleanOrderNo);
            }
            
            // 获取当前用户
            com.shop.online.entity.User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                log.error("获取当前用户失败，无法支付订单");
                throw new BusinessException("请先登录再支付订单");
            }
            Long userId = currentUser.getId();
//            log.info("当前用户ID: {}", userId);
            
            // 查询订单
//            log.info("查询订单信息，订单号: {}, 用户ID: {}", cleanOrderNo, userId);
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderNo, cleanOrderNo);
            queryWrapper.eq(Order::getUserId, userId);
            queryWrapper.eq(Order::getDeleted, 0); // 只查询未删除订单
            Order order = baseMapper.selectOne(queryWrapper);
            
            // 如果找不到订单，尝试只用订单号再查询一次（可能是因为userId不匹配）
            if (order == null) {
                log.warn("未找到订单（基于用户ID查询），尝试仅使用订单号查询，订单号: {}", cleanOrderNo);
                LambdaQueryWrapper<Order> orderNoQuery = new LambdaQueryWrapper<>();
                orderNoQuery.eq(Order::getOrderNo, cleanOrderNo);
                orderNoQuery.eq(Order::getDeleted, 0);
                order = baseMapper.selectOne(orderNoQuery);
                
                if (order != null) {
                    log.warn("找到订单，但不属于当前用户，订单用户ID: {}, 当前用户ID: {}", order.getUserId(), userId);
                    throw new BusinessException("订单不存在或不属于当前用户");
                }
            }
            
            if (order == null) {
                log.error("订单不存在，订单号: {}", cleanOrderNo);
                throw new BusinessException("订单不存在，请重新提交订单");
            }
            
            log.info("找到订单: ID={}, 订单号={}, 状态={}", order.getId(), order.getOrderNo(), order.getStatus());
            
            if (order.getStatus() != 0) {
                log.error("订单状态不正确，当前状态: {}, 需要状态: 0", order.getStatus());
                throw new BusinessException("订单状态不正确，只能支付待付款订单");
            }
            
            // 更新订单状态
            order.setStatus(1); // 待发货
            order.setUpdatedTime(LocalDateTime.now());
            
//            log.info("更新订单状态: 待付款(0) -> 待发货(1)");
            baseMapper.updateById(order);
//            log.info("订单支付成功，订单号: {}", cleanOrderNo);
        } catch (BusinessException e) {
            log.error("支付订单业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("支付订单失败", e);
            throw new BusinessException("支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        // 获取当前用户
        Long userId = userService.getCurrentUser().getId();
        
        // 查询订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.eq(Order::getDeleted, 0); // 只查询未删除订单
        Order order = baseMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款订单");
        }
        
        // 更新订单状态
        order.setStatus(4); // 已取消
        order.setUpdatedTime(LocalDateTime.now());
        
        baseMapper.updateById(order);
        
        // 恢复库存
        List<OrderItemDTO> orderItems = orderItemMapper.selectByOrderId(order.getId());
        
        for (OrderItemDTO orderItem : orderItems) {
            Product product = productMapper.selectById(orderItem.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 确认收货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(String orderNo) {
        // 获取当前用户
        Long userId = userService.getCurrentUser().getId();
        
        // 查询订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.eq(Order::getDeleted, 0); // 只查询未删除订单
        Order order = baseMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 修改判断条件，应该是待收货(状态2)的订单才能确认收货
        if (order.getStatus() != 2) {
            throw new BusinessException("只能确认待收货订单");
        }
        
        // 更新订单状态
        order.setStatus(3); // 已完成
        order.setUpdatedTime(LocalDateTime.now());
        
        baseMapper.updateById(order);
        
//        log.info("订单确认收货成功: {}", orderNo);
    }

    /**
     * 获取订单详情
     */
    @Override
    public OrderVO getOrderDetail(String orderNo) {
        // 获取当前用户
        Long userId = userService.getCurrentUser().getId();
        
        // 查询订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.eq(Order::getDeleted, 0); // 只查询未删除订单
        Order order = baseMapper.selectOne(queryWrapper);
        
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 查询订单项
        List<OrderItemDTO> orderItems = orderItemMapper.selectByOrderId(order.getId());
        
        // 转换VO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setStatus(order.getStatus().toString());
        
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
        
        return orderVO;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 生成订单号规则：年月日时分秒+4位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateTime = LocalDateTime.now().format(formatter);
        
        // 4位随机数
        Random random = new Random();
        int randomNum = random.nextInt(9000) + 1000;
        
        return dateTime + randomNum;
    }
    
    /**
     * 获取卖家订单列表
     */
    @Override
    public PageResult<OrderVO> getSellerOrders(Map<String, Object> params) {
//        log.info("获取卖家订单列表，查询参数: {}", params);
        
        try {
            // 获取参数
            Long sellerId = Long.parseLong(params.get("sellerId").toString());
            Integer page = Integer.parseInt(params.getOrDefault("page", 1).toString());
            Integer size = Integer.parseInt(params.getOrDefault("size", 10).toString());
            String status = (String) params.get("status");
            
//            log.info("查询参数: sellerId={}, page={}, size={}, status={}", sellerId, page, size, status);
            
            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getSellerId, sellerId);
            queryWrapper.eq(Order::getDeleted, 0);  // 未删除
            
            // 按状态筛选
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Order::getStatus, Integer.parseInt(status));
            }
            
            // 按创建时间倒序
            queryWrapper.orderByDesc(Order::getCreatedTime);
            
            // 分页查询
            Page<Order> pageParam = new Page<>(page, size);
            log.info("执行订单分页查询: 页码={}, 每页数量={}", page, size);
            Page<Order> orderPage = baseMapper.selectPage(pageParam, queryWrapper);
            
            log.info("查询结果: 总条数={}, 记录数量={}", orderPage.getTotal(), orderPage.getRecords().size());
            
            if (CollectionUtils.isEmpty(orderPage.getRecords())) {
                log.info("查询结果为空，返回空列表");
                return PageResult.of(orderPage.getTotal(), new ArrayList<>());
            }
            
            // 查询订单项
            List<Long> orderIds = orderPage.getRecords().stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());
            
            List<OrderItemDTO> orderItems = orderItemMapper.selectByOrderIds(orderIds);
//            log.info("查询到订单项数量: {}", orderItems.size());
            
            // 按订单ID分组
            Map<Long, List<OrderItemDTO>> orderItemMap = orderItems.stream()
                    .collect(Collectors.groupingBy(OrderItemDTO::getOrderId));
            
            // 转换VO
            List<OrderVO> orderVOList = orderPage.getRecords().stream().map(order -> {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                
                // 转换订单状态
                orderVO.setStatus(order.getStatus().toString());
                
                // 确保时间字段不为null
                if (order.getCreatedTime() != null) {
                    // 设置创建时间（这里是使用LocalDateTime类型）
                    orderVO.setCreateTime(order.getCreatedTime());
                    log.debug("订单创建时间设置成功: {}", order.getCreatedTime());
                } else {
                    // 如果为null，设置为当前时间
                    orderVO.setCreateTime(LocalDateTime.now());
                    log.warn("订单创建时间为null，设置为当前时间");
                }
                
                // 封装订单商品
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
                
                return orderVO;
            }).collect(Collectors.toList());
            
            PageResult<OrderVO> result = PageResult.of(orderPage.getTotal(), orderVOList);
//            log.info("返回卖家订单列表结果: 总数={}, 条目数={}", result.getTotal(), result.getList().size());
            return result;
            
        } catch (Exception e) {
            log.error("获取卖家订单列表时发生异常", e);
            throw e;
        }
    }
    
    /**
     * 卖家发货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(String orderNo, Long sellerId) {
        log.info("卖家发货: orderNo={}, sellerId={}", orderNo, sellerId);
        
        // 查询订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        queryWrapper.eq(Order::getSellerId, sellerId);
        queryWrapper.eq(Order::getDeleted, 0);  // 未删除
        Order order = baseMapper.selectOne(queryWrapper);
        
        if (order == null) {
            log.error("订单不存在: {}", orderNo);
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != 1) {  // 1-待发货
            log.error("订单状态不正确, 当前状态: {}", order.getStatus());
            throw new BusinessException("只能发货待发货状态的订单");
        }
        
        // 更新订单状态
        order.setStatus(2);  // 2-已发货
        order.setUpdatedTime(LocalDateTime.now());
        
        int updated = baseMapper.updateById(order);
        log.info("订单状态已更新为已发货: {}", orderNo);
        
        return updated > 0;
    }
    
    /**
     * 统计卖家指定状态的订单数量
     */
    @Override
    public int countSellerOrdersByStatus(Long sellerId, Integer status) {
        log.info("统计卖家订单数量: sellerId={}, status={}", sellerId, status);
        
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getSellerId, sellerId);
        queryWrapper.eq(Order::getStatus, status);
        queryWrapper.eq(Order::getDeleted, 0);  // 未删除
        
        long count = baseMapper.selectCount(queryWrapper);
        log.info("卖家订单数量统计结果: {}", count);
        
        return Math.toIntExact(count);
    }
    
    /**
     * 统计卖家订单总数
     */
    @Override
    public int countSellerOrders(Long sellerId) {
        log.info("统计卖家订单总数: sellerId={}", sellerId);
        
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getSellerId, sellerId);
        queryWrapper.eq(Order::getDeleted, 0);  // 未删除
        
        long count = baseMapper.selectCount(queryWrapper);
        log.info("卖家订单总数统计结果: {}", count);
        
        return Math.toIntExact(count);
    }
} 