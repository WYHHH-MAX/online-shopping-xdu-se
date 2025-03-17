package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.entity.Product;
import com.shop.online.entity.ProductImage;
import com.shop.online.mapper.ProductImageMapper;
import com.shop.online.mapper.ProductMapper;
import com.shop.online.service.ProductService;
import com.shop.online.utils.BeanCopyUtils;
import com.shop.online.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        
        return new PageResult<>(pageResult.getTotal(), productVOList);
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
        
        return new PageResult<>(pageResult.getTotal(), productVOList);
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
        
        return new PageResult<>(productPage.getTotal(), productVOList);
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
        
        return new PageResult<>(productPage.getTotal(), productVOList);
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
        
        return new PageResult<>(productPage.getTotal(), productVOList);
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