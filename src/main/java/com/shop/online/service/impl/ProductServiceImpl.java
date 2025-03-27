package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.common.result.PageResult;
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
import java.io.File;
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
        log.info("根据分类ID查询商品: categoryId={}, page={}, size={}", categoryId, page, size);
        
        // 判断是否为一级分类 (parent_id = 0)
        boolean isPrimaryCategory = isPrimaryCategory(categoryId);
        log.info("分类 {} 是否为一级分类: {}", categoryId, isPrimaryCategory);
        
        // 如果是一级分类，获取其所有子分类ID
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId); // 包含自身

        if (isPrimaryCategory) {
            // 查询该一级分类下的所有二级分类
            List<Long> subCategoryIds = getSubCategoryIds(categoryId);
            log.info("一级分类 {} 的子分类IDs: {}", categoryId, subCategoryIds);
            categoryIds.addAll(subCategoryIds);
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Product::getCategoryId, categoryIds)
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
        log.info("根据分类ID查询商品(带排序): categoryId={}, page={}, size={}, sortBy={}", categoryId, page, size, sortBy);
        
        // 判断是否为一级分类 (parent_id = 0)
        boolean isPrimaryCategory = isPrimaryCategory(categoryId);
        log.info("分类 {} 是否为一级分类: {}", categoryId, isPrimaryCategory);
        
        // 如果是一级分类，获取其所有子分类ID
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId); // 包含自身

        if (isPrimaryCategory) {
            // 查询该一级分类下的所有二级分类
            List<Long> subCategoryIds = getSubCategoryIds(categoryId);
            log.info("一级分类 {} 的子分类IDs: {}", categoryId, subCategoryIds);
            categoryIds.addAll(subCategoryIds);
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Product::getCategoryId, categoryIds)
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
        log.info("根据条件查询商品: {}", queryDTO);
        
        Page<Product> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        // 处理分类ID查询
        if (queryDTO.getCategoryId() != null) {
            // 判断是否为一级分类
            boolean isPrimaryCategory = isPrimaryCategory(queryDTO.getCategoryId());
            log.info("分类 {} 是否为一级分类: {}", queryDTO.getCategoryId(), isPrimaryCategory);
            
            if (isPrimaryCategory) {
                // 获取所有子分类ID
                List<Long> categoryIds = new ArrayList<>();
                categoryIds.add(queryDTO.getCategoryId());  // 包含自身
                
                // 查询该一级分类下的所有二级分类
                List<Long> subCategoryIds = getSubCategoryIds(queryDTO.getCategoryId());
                log.info("一级分类 {} 的子分类IDs: {}", queryDTO.getCategoryId(), subCategoryIds);
                categoryIds.addAll(subCategoryIds);
                
                // 使用IN查询
                wrapper.in(Product::getCategoryId, categoryIds);
            } else {
                // 二级分类直接精确查询
                wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
            }
        }
        
        // 添加其他查询条件
        wrapper.like(StringUtils.hasText(queryDTO.getKeyword()), Product::getName, queryDTO.getKeyword())
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
        
        // 处理商品图片
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // 获取临时图片列表
            List<String> tempImages = product.getImages();
            log.info("商品临时图片列表: {}", tempImages);
            
            // 处理图片路径，将临时图片改名为正式格式 (productId_index.jpg)
            List<String> formalImages = new ArrayList<>();
            for (int i = 0; i < tempImages.size(); i++) {
                String tempPath = tempImages.get(i);
                
                // 是否已经是规范格式
                if (tempPath.matches(".*/"+product.getId()+"_\\d+\\..*")) {
                    log.info("图片已经是规范格式: {}", tempPath);
                    formalImages.add(tempPath);
                    continue;
                }
                
                String extension = tempPath.substring(tempPath.lastIndexOf("."));
                // 构建新的图片路径
                String newFileName = product.getId() + "_" + (i + 1) + extension;
                String newPath = "/images/products/" + newFileName;
                
                // 移动文件
                try {
                    String projectRoot = System.getProperty("user.dir");
                    String staticDir = projectRoot + "/src/main/resources/static";
                    
                    File sourceFile = new File(staticDir + tempPath);
                    File targetFile = new File(staticDir + newPath);
                    
                    // 确保目标目录存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    
                    if (sourceFile.exists()) {
                        boolean renamed = sourceFile.renameTo(targetFile);
                        log.info("重命名文件 {} 为 {}, 结果: {}", tempPath, newPath, renamed);
                        
                        // 添加到正式图片列表
                        formalImages.add(newPath);
                    } else {
                        log.warn("源文件不存在: {}", staticDir + tempPath);
                        formalImages.add(tempPath); // 如果源文件不存在，使用原路径
                    }
                } catch (Exception e) {
                    log.error("重命名文件时发生错误: {}", e.getMessage(), e);
                    formalImages.add(tempPath); // 发生错误时使用原路径
                }
            }
            
            // 设置第一张图片为主图
            if (!formalImages.isEmpty()) {
                product.setMainImage(formalImages.get(0));
                log.info("设置商品主图: {}", product.getMainImage());
            }
            
            // 更新商品主图
            this.updateById(product);
            
            // 保存图片关联关系
            saveProductImages(product.getId(), formalImages);
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
        
        // 如果有图片，更新商品图片
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // 获取临时图片列表
            List<String> tempImages = product.getImages();
            log.info("商品更新临时图片列表: {}", tempImages);
            
            // 处理图片路径，将临时图片改名为正式格式 (productId_index.jpg)
            List<String> formalImages = new ArrayList<>();
            
            // 获取当前图片数量（用于确定新序号）
            int currentImageCount = getProductImageCount(product.getId());
            log.info("当前商品图片数量: {}", currentImageCount);
            
            // 处理现有的图片路径
            for (int i = 0; i < tempImages.size(); i++) {
                String tempPath = tempImages.get(i);
                
                // 检查是否已经是规范格式
                if (tempPath.matches(".*/"+product.getId()+"_\\d+\\..*")) {
                    log.info("图片已经是规范格式: {}", tempPath);
                    formalImages.add(tempPath);
                    continue;
                }
                
                // 处理新上传的图片，需要重命名
                String extension = tempPath.substring(tempPath.lastIndexOf("."));
                // 构建新的图片路径，新图片序号从现有数量+1开始
                String newFileName = product.getId() + "_" + (currentImageCount + i + 1) + extension;
                String newPath = "/images/products/" + newFileName;
                
                // 移动文件
                try {
                    String projectRoot = System.getProperty("user.dir");
                    String staticDir = projectRoot + "/src/main/resources/static";
                    
                    File sourceFile = new File(staticDir + tempPath);
                    File targetFile = new File(staticDir + newPath);
                    
                    // 确保目标目录存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    
                    if (sourceFile.exists()) {
                        boolean renamed = sourceFile.renameTo(targetFile);
                        log.info("重命名文件 {} 为 {}, 结果: {}", tempPath, newPath, renamed);
                        
                        // 添加到正式图片列表
                        formalImages.add(newPath);
                    } else {
                        log.warn("源文件不存在: {}", staticDir + tempPath);
                        formalImages.add(tempPath); // 如果源文件不存在，使用原路径
                    }
                } catch (Exception e) {
                    log.error("重命名文件时发生错误: {}", e.getMessage(), e);
                    formalImages.add(tempPath); // 发生错误时使用原路径
                }
            }
            
            // 设置第一张图片为主图
            if (!formalImages.isEmpty()) {
                product.setMainImage(formalImages.get(0));
                log.info("更新商品主图: {}", product.getMainImage());
            }
            
            // 更新商品
            this.updateById(product);
            
            // 更新商品图片关联
            // 先删除旧图片
            deleteProductImages(product.getId());
            // 保存新图片
            saveProductImages(product.getId(), formalImages);
        } else {
            // 更新商品基本信息
            this.updateById(product);
        }
        
        log.info("商品更新成功, id={}", product.getId());
        
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

    @Override
    public int getProductImageCount(Long productId) {
        if (productId == null) {
            return 0;
        }
        
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId)
               .eq(ProductImage::getDeleted, 0);
               
        return Math.toIntExact(productImageMapper.selectCount(wrapper));
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        
        // 加载商品图片
        List<String> images = getProductImages(product.getId());
        vo.setImages(images);
        
        return vo;
    }

    /**
     * 判断是否为一级分类
     */
    private boolean isPrimaryCategory(Long categoryId) {
        Integer count = baseMapper.isCategoryPrimary(categoryId);
        return count != null && count > 0;
    }

    /**
     * 获取一级分类下的所有二级分类ID
     */
    private List<Long> getSubCategoryIds(Long parentId) {
        List<Long> subCategoryIds = baseMapper.getSubCategoryIds(parentId);
        return subCategoryIds != null ? subCategoryIds : new ArrayList<>();
    }
} 