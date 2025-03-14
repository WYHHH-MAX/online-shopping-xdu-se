package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.entity.Product;
import com.shop.online.entity.ProductImage;
import com.shop.online.mapper.ProductMapper;
import com.shop.online.mapper.ProductImageMapper;
import com.shop.online.service.ProductService;
import com.shop.online.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    @Override
    public PageResult<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        log.info("Getting products for category: {}, page: {}, size: {}", categoryId, page, size);
        
        Page<Product> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
              .eq(Product::getStatus, 1)
              .eq(Product::getDeleted, 0)  // 添加未删除条件
              .orderByDesc(Product::getSales);
        
        Page<Product> productPage = this.page(pageParam, wrapper);
        log.info("Found {} products in total", productPage.getTotal());
        
        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            log.info("Processing product: {}", product);
            ProductVO vo = convertToVO(product);
            List<String> images = getProductImages(product.getId());
            log.info("Found {} images for product {}", images.size(), product.getId());
            vo.setImages(images);
            productVOList.add(vo);
        }
        
        return new PageResult<>(productPage.getTotal(), productVOList);
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
        return vo;
    }
} 