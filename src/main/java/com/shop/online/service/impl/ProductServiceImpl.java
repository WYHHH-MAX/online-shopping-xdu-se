package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.common.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.entity.Product;
import com.shop.online.entity.ProductImage;
import com.shop.online.exception.BusinessException;
import com.shop.online.mapper.ProductImageMapper;
import com.shop.online.mapper.ProductMapper;
import com.shop.online.service.ProductService;
import com.shop.online.utils.BeanCopyUtils;
import com.shop.online.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private ProductImageMapper productImageMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageResult<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getDeleted, 0)  // 添加未删除条件
                .eq(Product::getStatus, 1);  // 只查询上架商品
        
        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> pageResult = baseMapper.selectPage(productPage, queryWrapper);
        
        // 转换为VO
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : pageResult.getRecords()) {
            ProductVO vo = convertToVO(product);
            productVOList.add(vo);
        }
        
        return PageResult.of(pageResult.getTotal(), productVOList);
    }

    @Override
    public PageResult<ProductVO> getProductsByCategoryWithSort(Long categoryId, Integer page, Integer size, String sortBy) {
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getDeleted, 0)  // 添加未删除条件
                .eq(Product::getStatus, 1);  // 只查询上架商品
        
        // 排序
        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "price_asc":
                    queryWrapper.orderByAsc(Product::getPrice);
                    break;
                case "price_desc":
                    queryWrapper.orderByDesc(Product::getPrice);
                    break;
                case "sales_desc":
                    queryWrapper.orderByDesc(Product::getSales);
                    break;
                case "rating_desc":
                    // 替代rating排序，使用销量和创建时间排序
                    queryWrapper.orderByDesc(Product::getSales)
                            .orderByDesc(Product::getCreatedTime);
                    break;
                default:
                    queryWrapper.orderByDesc(Product::getCreatedTime);
                    break;
            }
        } else {
            queryWrapper.orderByDesc(Product::getCreatedTime);
        }
        
        // 分页查询
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> pageResult = baseMapper.selectPage(productPage, queryWrapper);
        
        // 转换为VO
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : pageResult.getRecords()) {
            ProductVO vo = convertToVO(product);
            productVOList.add(vo);
        }
        
        return PageResult.of(pageResult.getTotal(), productVOList);
    }

    @Override
    public PageResult<ProductVO> getProductsByCategory(Integer categoryId, Integer page, Integer size, String sortBy) {
        // 将Integer类型的categoryId转换为Long类型，然后调用已有的方法
        return getProductsByCategoryWithSort(categoryId.longValue(), page, size, sortBy);
    }

    @Override
    public PageResult<ProductVO> getProductsByCondition(ProductQueryDTO queryDTO) {
        Page<Product> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        wrapper.eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId())
              .like(StringUtils.hasText(queryDTO.getKeyword()), Product::getName, queryDTO.getKeyword())
              .ge(queryDTO.getMinPrice() != null, Product::getPrice, queryDTO.getMinPrice())
              .le(queryDTO.getMaxPrice() != null, Product::getPrice, queryDTO.getMaxPrice())
              .eq(Product::getStatus, 1);
        
        // 添加排序
        if (StringUtils.hasText(queryDTO.getOrderBy())) {
            wrapper.last("ORDER BY " + queryDTO.getOrderBy() + (queryDTO.getIsAsc() ? " ASC" : " DESC"));
        } else {
            wrapper.orderByDesc(Product::getSales);
        }
        
        Page<Product> productPage = this.page(pageParam, wrapper);
        
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            productVOList.add(convertToVO(product));
        }
        
        return PageResult.of(productPage.getTotal(), productVOList);
    }

    @Override
    public ProductVO getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            return null;
        }
        ProductVO vo = convertToVO(product);
        // 获取商品图片
        vo.setImages(getProductImages(id));
        return vo;
    }

    @Override
    public PageResult<ProductVO> getProductsBySeller(Long sellerId, Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, sellerId)
              .orderByDesc(Product::getCreatedTime);
        
        Page<Product> productPage = this.page(pageParam, wrapper);
        
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            productVOList.add(convertToVO(product));
        }
        
        return PageResult.of(productPage.getTotal(), productVOList);
    }

    @Override
    public PageResult<ProductVO> getFeaturedProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsFeatured, true)
              .eq(Product::getDeleted, 0)
              .eq(Product::getStatus, 1)
              .orderByAsc(Product::getFeaturedSort);

        Page<Product> productPage = this.page(pageParam, wrapper);
        
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            ProductVO vo = convertToVO(product);
            vo.setImages(getProductImages(product.getId()));
            productVOList.add(vo);
        }
        
        return PageResult.of(productPage.getTotal(), productVOList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO saveProduct(Product product) {
        log.info("保存商品: {}", product);
        
        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(1);  // 默认上架
        }
        if (product.getSales() == null) {
            product.setSales(0);  // 初始销量为0
        }
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(0);  // 默认不推荐
        }
        
        // 设置创建时间和更新时间
        product.setCreatedTime(LocalDateTime.now());
        product.setUpdatedTime(LocalDateTime.now());
        product.setDeleted(0);  // 未删除
        
        // 保存商品
        this.save(product);
        log.info("商品保存成功, id={}", product.getId());
        
        // 保存商品图片
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            saveProductImages(product.getId(), product.getImages());
        }
        
        // 返回VO
        return convertToVO(product);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(Product product) {
        log.info("更新商品: {}", product);
        
        // 检查商品是否存在
        Product existingProduct = this.getById(product.getId());
        if (existingProduct == null) {
            log.error("商品不存在, id={}", product.getId());
            throw new BusinessException("商品不存在");
        }
        
        // 保留不能修改的字段
        product.setCreatedTime(existingProduct.getCreatedTime());
        product.setDeleted(existingProduct.getDeleted());
        product.setSellerId(existingProduct.getSellerId());  // 卖家ID不允许修改
        
        // 设置更新时间
        product.setUpdatedTime(LocalDateTime.now());
        
        // 更新商品
        this.updateById(product);
        log.info("商品更新成功, id={}", product.getId());
        
        // 如果有图片，更新商品图片
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // 删除旧图片
            deleteProductImages(product.getId());
            // 保存新图片
            saveProductImages(product.getId(), product.getImages());
        }
        
        // 返回VO
        return convertToVO(product);
    }
    
    @Override
    public boolean updateStock(Long productId, Integer stock) {
        log.info("更新商品库存, productId={}, stock={}", productId, stock);
        
        if (stock < 0) {
            log.error("库存不能为负数");
            throw new BusinessException("库存不能为负数");
        }
        
        Product product = this.getById(productId);
        if (product == null) {
            log.error("商品不存在, id={}", productId);
            throw new BusinessException("商品不存在");
        }
        
        product.setStock(stock);
        product.setUpdatedTime(LocalDateTime.now());
        
        return this.updateById(product);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStock(Long sellerId, Map<Long, Integer> stockMap) {
        log.info("批量更新商品库存, sellerId={}, stockMap={}", sellerId, stockMap);
        
        if (stockMap == null || stockMap.isEmpty()) {
            log.warn("库存更新map为空");
            return false;
        }
        
        // 验证所有商品是否属于此卖家
        List<Long> productIds = new ArrayList<>(stockMap.keySet());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getId, productIds)
               .eq(Product::getSellerId, sellerId);
        
        List<Product> products = this.list(wrapper);
        
        // 商品数量不匹配
        if (products.size() != productIds.size()) {
            log.error("部分商品不存在或不属于此卖家, sellerId={}", sellerId);
            throw new BusinessException("部分商品不存在或不属于此卖家");
        }
        
        // 批量更新
        for (Product product : products) {
            Integer stock = stockMap.get(product.getId());
            if (stock != null && stock >= 0) {
                product.setStock(stock);
                product.setUpdatedTime(LocalDateTime.now());
                this.updateById(product);
                log.debug("更新商品库存成功, id={}, stock={}", product.getId(), stock);
            } else {
                log.warn("商品库存无效, id={}, stock={}", product.getId(), stock);
            }
        }
        
        return true;
    }
    
    @Override
    public int countSellerProducts(Long sellerId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, sellerId)
               .eq(Product::getDeleted, 0);
        
        return Math.toIntExact(this.count(wrapper));
    }
    
    @Override
    public int countSellerLowStockProducts(Long sellerId, Integer threshold) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, sellerId)
               .eq(Product::getDeleted, 0)
               .le(Product::getStock, threshold);
        
        return Math.toIntExact(this.count(wrapper));
    }

    @Override
    public boolean deleteProduct(Long productId) {
        log.info("删除商品: id={}", productId);
        
        Product product = this.getById(productId);
        if (product == null) {
            log.error("商品不存在, id={}", productId);
            throw new BusinessException("商品不存在");
        }
        
        // 逻辑删除
        product.setDeleted(1);
        product.setStatus(0);  // 下架
        product.setUpdatedTime(LocalDateTime.now());
        
        return this.updateById(product);
    }

    /**
     * 保存商品图片
     */
    private void saveProductImages(Long productId, List<String> images) {
        for (int i = 0; i < images.size(); i++) {
            ProductImage productImage = new ProductImage();
            productImage.setProductId(productId);
            productImage.setImageUrl(images.get(i));
            productImage.setSort(i);  // 按列表顺序排序
            productImage.setDeleted(0);
            
            productImageMapper.insert(productImage);
        }
    }
    
    /**
     * 删除商品图片
     */
    private void deleteProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId);
        
        productImageMapper.delete(wrapper);
    }

    private List<String> getProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId)
               .eq(ProductImage::getDeleted, 0)  // 添加未删除条件
               .orderByAsc(ProductImage::getSort);
        
        List<ProductImage> images = productImageMapper.selectList(wrapper);
        return images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        
        // 加载商品图片
        List<String> images = getProductImages(product.getId());
        vo.setImages(images);
        
        return vo;
    }
} 