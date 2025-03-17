package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.common.PageResult;
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
        // 构建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        
        // 获取当前用户ID
        Long userId = userService.getCurrentUser().getId();
        queryWrapper.eq(Order::getUserId, userId);
        
        // 根据状态筛选
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        
        // 只查询未删除的订单
        queryWrapper.eq(Order::getDeleted, 0);
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Order::getCreatedTime);
        
        // 分页查询
        Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<Order> orderPage = baseMapper.selectPage(page, queryWrapper);
        
        if (CollectionUtils.isEmpty(orderPage.getRecords())) {
            return PageResult.of(orderPage.getTotal(), new ArrayList<>());
        }
        
        // 查询订单项
        List<Long> orderIds = orderPage.getRecords().stream()
                .map(Order::getId)
                .collect(Collectors.toList());
        
        // 查询订单对应的订单项
        List<OrderItemDTO> orderItems = orderItemMapper.selectByOrderIds(orderIds);
        
        // 按订单ID分组
        Map<Long, List<OrderItemDTO>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemDTO::getOrderId));
        
        // 转换VO
        List<OrderVO> orderVOList = orderPage.getRecords().stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            
            // 转换订单状态
            orderVO.setStatus(order.getStatus().toString());
            
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
        
        return PageResult.of(orderPage.getTotal(), orderVOList);
    }

    /**
     * 创建订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO createOrderDTO) {
        log.info("开始创建订单: {}", createOrderDTO);
        
        // 验证参数
        List<Integer> cartItemIds = createOrderDTO.getCartItemIds();
        if (CollectionUtils.isEmpty(cartItemIds)) {
            log.error("购物车项为空");
            throw new BusinessException("请选择要购买的商品");
        }
        
        try {
            // 获取当前用户
            Long userId = userService.getCurrentUser().getId();
            log.info("当前用户ID: {}", userId);
            
            // 查询购物车项
            LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Cart::getId, cartItemIds.stream().map(Integer::longValue).collect(Collectors.toList()));
            queryWrapper.eq(Cart::getUserId, userId);
            queryWrapper.eq(Cart::getDeleted, 0); // 只查询未删除的购物车项
            List<Cart> cartItems = cartMapper.selectList(queryWrapper);
            log.info("查询到的购物车项: {}", cartItems);
            
            if (CollectionUtils.isEmpty(cartItems) || cartItems.size() != cartItemIds.size()) {
                log.error("部分商品不在购物车中, 预期数量: {}, 实际数量: {}", 
                         cartItemIds.size(), cartItems != null ? cartItems.size() : 0);
                throw new BusinessException("部分商品不在购物车中");
            }
            
            // 查询商品信息
            List<Long> productIds = cartItems.stream()
                    .map(Cart::getProductId)
                    .collect(Collectors.toList());
            log.info("查询商品信息, 商品IDs: {}", productIds);
            
            List<Product> products = productMapper.selectBatchIds(productIds);
            log.info("查询到的商品: {}", products);
            
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            
            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            // 确保有卖家信息（这里简单处理，假设所有商品来自同一卖家，取第一个商品的卖家）
            Long sellerId = null;
            
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
                    log.info("订单卖家ID: {}", sellerId);
                }
            }
            
            if (sellerId == null) {
                log.error("无法确定卖家信息");
                throw new BusinessException("无法确定卖家信息");
            }
            
            // 生成订单号
            String orderNo = generateOrderNo();
            log.info("生成订单号: {}", orderNo);
            
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
            
            log.info("保存订单: {}", order);
            baseMapper.insert(order);
            log.info("订单已保存, ID: {}", order.getId());
            
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
                log.debug("商品库存已更新, 商品: {}, 新库存: {}", product.getName(), product.getStock());
            }
            
            log.info("保存订单项, 数量: {}", orderItems.size());
            orderItemMapper.insertBatch(orderItems);
            
            // 删除购物车项
            List<Long> longCartItemIds = cartItemIds.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());
            log.info("删除购物车项, IDs: {}", longCartItemIds);
            cartMapper.deleteBatchIds(longCartItemIds);
            
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
            
            log.info("订单创建成功: {}", orderVO);
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
            throw new BusinessException("订单状态不正确");
        }
        
        // 更新订单状态
        order.setStatus(1); // 待发货
        order.setUpdatedTime(LocalDateTime.now());
        
        baseMapper.updateById(order);
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
        
        if (order.getStatus() != 2) {
            throw new BusinessException("只能确认待收货订单");
        }
        
        // 更新订单状态
        order.setStatus(3); // 已完成
        order.setUpdatedTime(LocalDateTime.now());
        
        baseMapper.updateById(order);
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
} 