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
import com.shop.online.util.FileUtil;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
        // 判断是否为一级分类 (parent_id = 0)
        boolean isPrimaryCategory = isPrimaryCategory(categoryId);
        
        // 如果是一级分类，获取其所有子分类ID
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId); // 包含自身

        if (isPrimaryCategory) {
            // 查询该一级分类下的所有二级分类
            List<Long> subCategoryIds = getSubCategoryIds(categoryId);
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
            // 加载产品图片
            vo.setImages(getProductImages(product.getId()));
            productVOList.add(vo);
        }
        
        return PageResult.of(pageResult.getTotal(), productVOList);
    }

    @Override
    public PageResult<ProductVO> getProductsByCategoryWithSort(Long categoryId, Integer page, Integer size, String sortBy) {
        // 判断是否为一级分类 (parent_id = 0)
        boolean isPrimaryCategory = isPrimaryCategory(categoryId);
        
        // 如果是一级分类，获取其所有子分类ID
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId); // 包含自身

        if (isPrimaryCategory) {
            // 查询该一级分类下的所有二级分类
            List<Long> subCategoryIds = getSubCategoryIds(categoryId);
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
            // 加载产品图片
            vo.setImages(getProductImages(product.getId()));
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
        
        // 处理分类ID查询
        if (queryDTO.getCategoryId() != null) {
            // 判断是否为一级分类
            boolean isPrimaryCategory = isPrimaryCategory(queryDTO.getCategoryId());
            
            if (isPrimaryCategory) {
                // 获取所有子分类ID
                List<Long> categoryIds = new ArrayList<>();
                categoryIds.add(queryDTO.getCategoryId());  // 包含自身
                
                // 查询该一级分类下的所有二级分类
                List<Long> subCategoryIds = getSubCategoryIds(queryDTO.getCategoryId());
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
            ProductVO vo = convertToVO(product);
            // 加载产品图片
            vo.setImages(getProductImages(product.getId()));
            productVOList.add(vo);
        }
        
        return PageResult.of(productPage.getTotal(), productVOList);
    }

    @Override
    public ProductVO getProductDetail(Long id) {
        log.info("获取商品详情: id={}", id);
        
        Product product = this.getById(id);
        if (product == null || product.getDeleted() == 1) {
            log.error("商品不存在, id={}", id);
            return null;
        }
        
        ProductVO vo = convertToVO(product);
        
        // 获取未删除的图片
        vo.setImages(getProductImages(id));
        
        // 确保主图也包含在images列表中
        if (product.getMainImage() != null && !vo.getImages().contains(product.getMainImage())) {
            log.warn("主图不在未删除图片列表中，可能已被删除: mainImage={}", product.getMainImage());
            
            // 如果有其他未删除的图片，将第一张设为主图
            if (!vo.getImages().isEmpty()) {
                log.info("将第一张未删除图片设为主图: {}", vo.getImages().get(0));
                product.setMainImage(vo.getImages().get(0));
                product.setUpdatedTime(LocalDateTime.now());
                this.updateById(product);
                vo.setMainImage(vo.getImages().get(0));
            } else {
                // 没有未删除的图片
                log.warn("没有未删除的图片，清空主图");
                product.setMainImage(null);
                product.setUpdatedTime(LocalDateTime.now());
                this.updateById(product);
                vo.setMainImage(null);
            }
        }
        
        log.info("商品详情获取成功: id={}, name={}, mainImage={}, images={}",
                id, product.getName(), product.getMainImage(), vo.getImages());
        
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
            ProductVO vo = convertToVO(product);
            // 加载产品图片
            vo.setImages(getProductImages(product.getId()));
            productVOList.add(vo);
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
            // 加载产品图片
            vo.setImages(getProductImages(product.getId()));
            productVOList.add(vo);
        }
        
        return PageResult.of(productPage.getTotal(), productVOList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO saveProduct(Product product) {
        log.info("开始保存新商品");
        
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
        
        // 保存商品基本信息
        this.save(product);
        log.info("商品基本信息保存成功, id={}", product.getId());
        
        // ----- 图片处理流程开始 -----
        // 获取项目根路径和静态资源目录
        String projectRoot = System.getProperty("user.dir");
        String staticDir = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static";
        String productsDir = staticDir + File.separator + "images" + File.separator + "products";
        
        log.info("图片保存目录: {}", productsDir);
        
        // 清空主图，将在处理新图片时重新设置
        product.setMainImage(null);
        
        // 处理新的图片列表
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // 去除重复的图片URL
            List<String> newImageUrls = new ArrayList<>(new LinkedHashSet<>(product.getImages()));
            log.info("要保存的新图片列表 (去重后共{}张): {}", newImageUrls.size(), newImageUrls);
            
            // 检查是否包含有效的图片URL
            boolean hasValidImages = false;
            for (String url : newImageUrls) {
                if (url != null && (url.startsWith("/images/") || url.startsWith("/api/images/"))) {
                    hasValidImages = true;
                    break;
                }
            }
            
            if (!hasValidImages) {
                log.warn("没有有效的图片URL，清空主图并更新商品");
                this.updateById(product);
                
                // 返回更新后的商品，确保不包含已删除的图片
                ProductVO vo = new ProductVO();
                BeanUtils.copyProperties(product, vo);
                vo.setImages(new ArrayList<>()); // 返回空图片列表
                return vo;
            }
            
            // 处理图片并直接重命名为正式格式
            List<String> formalImageUrls = new ArrayList<>();
            
            for (int i = 0; i < newImageUrls.size(); i++) {
                String imageUrl = newImageUrls.get(i);
                try {
                    // 处理不同格式的URL
                    if (imageUrl.startsWith("/api/images/")) {
                        imageUrl = imageUrl.substring("/api".length());
                    }
                    
                    // 跳过不符合格式的URL
                    if (!imageUrl.startsWith("/images/")) {
                        log.warn("图片URL格式不符，跳过: {}", imageUrl);
                        continue;
                    }
                    
                    // 获取文件扩展名
                    String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
                    
                    // 源文件路径
                    File sourceFile = new File(staticDir + imageUrl);
                    if (!sourceFile.exists()) {
                        log.warn("源图片文件不存在，跳过: {}", sourceFile.getAbsolutePath());
                        continue;
                    }
                    
                    // 直接使用正式命名（不再使用临时目录）
                    int formalIndex = i + 1; // 图片索引从1开始
                    String formalFileName = product.getId() + "_" + formalIndex + extension;
                    File formalFile = new File(productsDir, formalFileName);
                    
                    log.info("直接保存到正式位置: {} -> {}", sourceFile.getAbsolutePath(), formalFile.getAbsolutePath());
                    
                    // 复制到正式位置（使用REPLACE_EXISTING确保覆盖可能存在的同名文件）
                    Files.copy(sourceFile.toPath(), formalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // 记录正式URL
                    String formalUrl = "/images/products/" + formalFileName;
                    formalImageUrls.add(formalUrl);
                    log.info("成功保存并重命名图片: {}", formalUrl);
                    
                } catch (Exception e) {
                    log.error("处理图片失败: {}", e.getMessage(), e);
                }
            }
            
            log.info("所有处理后的图片URL: {}", formalImageUrls);
            
            // 保存图片记录到数据库
            if (!formalImageUrls.isEmpty()) {
                for (int i = 0; i < formalImageUrls.size(); i++) {
                    ProductImage productImage = new ProductImage();
                    productImage.setProductId(product.getId());
                    productImage.setImageUrl(formalImageUrls.get(i));
                    productImage.setSort(i);
                    productImage.setDeleted(0);
                    productImage.setCreatedTime(LocalDateTime.now());
                    productImage.setUpdatedTime(LocalDateTime.now());
                    
                    int insertResult = productImageMapper.insert(productImage);
                    log.info("保存图片记录到数据库: {}, 结果: {}", formalImageUrls.get(i), insertResult > 0 ? "成功" : "失败");
                }
                
                // 设置第一张图片为主图
                product.setMainImage(formalImageUrls.get(0));
                log.info("设置商品主图: {}", product.getMainImage());
            } else {
                log.warn("没有成功处理任何图片，主图保持为null");
            }
        } else {
            log.info("没有提供新图片，主图保持为null");
        }
        
        // 更新主图信息
        this.updateById(product);
        log.info("更新商品主图信息完成");
        
        // 返回VO
        ProductVO vo = convertToVO(product);
        // 加载最新的未删除图片
        vo.setImages(getProductImages(product.getId()));
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(Product product) {
        log.info("开始更新商品: id={}", product.getId());
        
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
        
        // ----- 图片处理流程开始 -----
        
        // 获取项目根路径和静态资源目录
        String projectRoot = System.getProperty("user.dir");
        String staticDir = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static";
        String productsDir = staticDir + File.separator + "images" + File.separator + "products";
        
        log.info("图片保存目录: {}", productsDir);
        
        // 获取更新前商品未删除的图片
        List<String> oldImages = getProductImages(product.getId());
        log.info("更新前商品 {} 的未删除图片: {}", product.getId(), oldImages);
        
        // 直接数据库硬删除旧图片记录，而不是标记为deleted=1
        try {
            LambdaQueryWrapper<ProductImage> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ProductImage::getProductId, product.getId());
            int deletedRows = productImageMapper.delete(deleteWrapper);
            log.info("已直接从数据库删除商品 {} 的所有图片记录，共 {} 条", product.getId(), deletedRows);
        } catch (Exception e) {
            log.error("删除商品图片记录失败", e);
            throw new BusinessException("删除图片记录失败: " + e.getMessage());
        }
        
        // 清空主图，将在处理新图片时重新设置
        product.setMainImage(null);
        
        // 处理新的图片列表
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // 去除重复的图片URL
            List<String> newImageUrls = new ArrayList<>(new LinkedHashSet<>(product.getImages()));
            log.info("要保存的新图片列表 (去重后共{}张): {}", newImageUrls.size(), newImageUrls);
            
            // 检查是否包含有效的图片URL
            boolean hasValidImages = false;
            for (String url : newImageUrls) {
                if (url != null && (url.startsWith("/images/") || url.startsWith("/api/images/"))) {
                    hasValidImages = true;
                    break;
                }
            }
            
            if (!hasValidImages) {
                log.warn("没有有效的图片URL，清空主图并更新商品");
                this.updateById(product);
                
                // 返回更新后的商品，确保不包含已删除的图片
                ProductVO vo = new ProductVO();
                BeanUtils.copyProperties(product, vo);
                vo.setImages(new ArrayList<>()); // 返回空图片列表
                return vo;
            }
            
            // 处理图片并直接重命名为正式格式
            List<String> formalImageUrls = new ArrayList<>();
            
            for (int i = 0; i < newImageUrls.size(); i++) {
                String imageUrl = newImageUrls.get(i);
                try {
                    // 处理不同格式的URL
                    if (imageUrl.startsWith("/api/images/")) {
                        imageUrl = imageUrl.substring("/api".length());
                    }
                    
                    // 跳过不符合格式的URL
                    if (!imageUrl.startsWith("/images/")) {
                        log.warn("图片URL格式不符，跳过: {}", imageUrl);
                        continue;
                    }
                    
                    // 获取文件扩展名
                    String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
                    
                    // 源文件路径
                    File sourceFile = new File(staticDir + imageUrl);
                    if (!sourceFile.exists()) {
                        log.warn("源图片文件不存在，跳过: {}", sourceFile.getAbsolutePath());
                        continue;
                    }
                    
                    // 直接使用正式命名（不再使用临时目录）
                    int formalIndex = i + 1; // 图片索引从1开始
                    String formalFileName = product.getId() + "_" + formalIndex + extension;
                    File formalFile = new File(productsDir, formalFileName);
                    
                    log.info("直接保存到正式位置: {} -> {}", sourceFile.getAbsolutePath(), formalFile.getAbsolutePath());
                    
                    // 复制到正式位置（使用REPLACE_EXISTING确保覆盖可能存在的同名文件）
                    Files.copy(sourceFile.toPath(), formalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // 记录正式URL
                    String formalUrl = "/images/products/" + formalFileName;
                    formalImageUrls.add(formalUrl);
                    log.info("成功保存并重命名图片: {}", formalUrl);
                    
                } catch (Exception e) {
                    log.error("处理图片失败: {}", e.getMessage(), e);
                }
            }
            
            log.info("所有处理后的图片URL: {}", formalImageUrls);
            
            // 保存图片记录到数据库
            if (!formalImageUrls.isEmpty()) {
                for (int i = 0; i < formalImageUrls.size(); i++) {
                    ProductImage productImage = new ProductImage();
                    productImage.setProductId(product.getId());
                    productImage.setImageUrl(formalImageUrls.get(i));
                    productImage.setSort(i);
                    productImage.setDeleted(0);
                    productImage.setCreatedTime(LocalDateTime.now());
                    productImage.setUpdatedTime(LocalDateTime.now());
                    
                    int insertResult = productImageMapper.insert(productImage);
                    log.info("保存图片记录到数据库: {}, 结果: {}", formalImageUrls.get(i), insertResult > 0 ? "成功" : "失败");
                }
                
                // 设置第一张图片为主图
                product.setMainImage(formalImageUrls.get(0));
                log.info("设置商品主图: {}", product.getMainImage());
            } else {
                log.warn("没有成功处理任何图片，主图保持为null");
            }
        } else {
            log.info("没有提供新图片，主图保持为null");
        }
        
        // 更新商品信息
        boolean updated = this.updateById(product);
        log.info("商品更新结果: {}", updated);
        
        // 获取更新后的图片列表（只包含未删除的图片）
        List<String> newImages = getProductImages(product.getId());
        log.info("更新后商品 {} 的图片: {}", product.getId(), newImages);
        
        // 返回VO
        ProductVO vo = convertToVO(product);
        vo.setImages(newImages);
        return vo;
    }
    
    @Override
    public boolean updateStock(Long productId, Integer stock) {
//        log.info("更新商品库存, productId={}, stock={}", productId, stock);
        
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
//        log.info("批量更新商品库存, sellerId={}, stockMap={}", sellerId, stockMap);
        
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
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long productId) {
        log.info("开始删除商品: id={}", productId);
        
        // 检查商品是否存在
        Product product = this.getById(productId);
        if (product == null) {
            log.error("商品不存在, id={}", productId);
            throw new BusinessException("商品不存在");
        }
        
        // 1. 获取商品现有的未删除图片
        List<String> oldImages = getProductImages(productId);
        log.info("删除前商品 {} 的未删除图片: {}", productId, oldImages);
        
        // 2. 查询所有未删除的图片记录，将它们标记为deleted=1
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                   .eq(ProductImage::getDeleted, 0);
        List<ProductImage> oldImageRecords = productImageMapper.selectList(queryWrapper);
        log.info("找到商品 {} 的 {} 张未删除图片记录", productId, oldImageRecords.size());
        
        // 逐条更新图片记录，标记为deleted=1
        for (ProductImage image : oldImageRecords) {
            image.setDeleted(1);
            image.setUpdatedTime(LocalDateTime.now());
            productImageMapper.updateById(image);
            log.info("已将图片记录标记为deleted=1: id={}, url={}", image.getId(), image.getImageUrl());
        }
        log.info("已将商品 {} 的所有图片记录标记为deleted=1，共 {} 张", productId, oldImageRecords.size());
        
        // 3. 逻辑删除商品记录
        product.setDeleted(1);
        product.setStatus(0);  // 下架
        product.setUpdatedTime(LocalDateTime.now());
        boolean result = this.updateById(product);
        log.info("商品 {} 逻辑删除结果: {}", productId, result);
        
        return result;
    }

    /**
     * 获取商品图片列表
     * @param productId 商品ID
     * @return 图片URL列表
     */
    private List<String> getProductImages(Long productId) {
        // 只查询未删除的图片记录
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId)
               .eq(ProductImage::getDeleted, 0)  // 明确只查询deleted=0的图片
               .orderByAsc(ProductImage::getSort);
        
        List<ProductImage> images = productImageMapper.selectList(wrapper);
        log.info("获取商品 {} 的图片: 找到 {} 张未删除图片(deleted=0)", productId, images.size());

        // 检查是否有任何图片记录的deleted字段不是0
        LambdaQueryWrapper<ProductImage> deletedWrapper = new LambdaQueryWrapper<>();
        deletedWrapper.eq(ProductImage::getProductId, productId)
                     .eq(ProductImage::getDeleted, 1);
        long deletedCount = productImageMapper.selectCount(deletedWrapper);
        log.info("商品 {} 有 {} 张已删除图片(deleted=1)", productId, deletedCount);

        // 转换为URL列表
        List<String> imageUrls = images.stream()
                .map(image -> {
                    log.debug("图片ID: {}, URL: {}, deleted: {}", 
                              image.getId(), image.getImageUrl(), image.getDeleted());
                    
                    // 确保图片URL不带/api前缀
                    String imageUrl = image.getImageUrl();
                    if (imageUrl.startsWith("/api/")) {
                        imageUrl = imageUrl.substring("/api".length());
                        log.debug("已移除图片URL前缀: {}", imageUrl);
                    }
                    
                    return imageUrl;
                })
                .collect(Collectors.toList());
        
        // 打印所有图片URL，方便调试
        if (!imageUrls.isEmpty()) {
            log.info("商品 {} 的未删除图片URL: {}", productId, imageUrls);
        }
        
        return imageUrls;
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
        if (product == null) {
            return null;
        }
        
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        
        // 在默认转换中不加载图片，让调用方决定是否需要加载图片
        // 这样避免重复加载图片的问题
        
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

    /**
     * 验证商品是否属于指定卖家
     */
    @Override
    public boolean isProductOwnedBySeller(Long productId, Long sellerId) {
        if (productId == null || sellerId == null) {
            return false;
        }
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, productId)
               .eq(Product::getSellerId, sellerId)
               .eq(Product::getDeleted, 0);
        
        return this.count(wrapper) > 0;
    }

    /**
     * 删除商品图片(根据URL)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProductImage(Long productId, String imageUrl) {
        log.info("删除商品图片: productId={}, imageUrl={}", productId, imageUrl);
        
        if (productId == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            log.error("参数无效: productId={}, imageUrl={}", productId, imageUrl);
            return false;
        }
        
        // 规范化图片URL，确保使用统一格式处理
        final String normalizedUrl = imageUrl.startsWith("/api/") ? 
            imageUrl.substring("/api".length()) : imageUrl;

        log.info("规范化后的图片URL: {}", normalizedUrl);
        
        // 查找匹配的图片记录 - 同时检查带/api前缀和不带前缀的URL
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId)
               .and(w -> w.eq(ProductImage::getImageUrl, normalizedUrl)
                         .or()
                         .eq(ProductImage::getImageUrl, "/api" + normalizedUrl))
               .eq(ProductImage::getDeleted, 0); // 只查询未删除的
        
        List<ProductImage> images = productImageMapper.selectList(wrapper);
        
        if (images.isEmpty()) {
            log.warn("要删除的图片不存在: productId={}, imageUrl={}", productId, imageUrl);
            return false;
        }
        
        // 逻辑删除第一张匹配的图片
        ProductImage image = images.get(0);
        image.setDeleted(1);
        image.setUpdatedTime(LocalDateTime.now());
        int result = productImageMapper.updateById(image);
        
        if (result > 0) {
            log.info("图片已标记为删除: id={}, url={}", image.getId(), image.getImageUrl());
            
            // 检查被删除的是否是主图，如果是则尝试将另一张图片设为主图
            Product product = this.getById(productId);
            if (product != null && (imageUrl.equals(product.getMainImage()) || 
                                   normalizedUrl.equals(product.getMainImage()) ||
                                   ("/api" + normalizedUrl).equals(product.getMainImage()))) {
                // 查找其他可用图片
                List<String> remainingImages = getProductImages(productId);
                
                if (!remainingImages.isEmpty()) {
                    // 将第一张可用图片设为主图
                    product.setMainImage(remainingImages.get(0));
                    product.setUpdatedTime(LocalDateTime.now());
                    this.updateById(product);
                    log.info("已将商品主图更新为: {}", remainingImages.get(0));
                } else {
                    // 没有可用图片，清空主图
                    product.setMainImage(null);
                    product.setUpdatedTime(LocalDateTime.now());
                    this.updateById(product);
                    log.warn("商品已没有可用图片，已清空主图");
                }
            }
            
            return true;
        } else {
            log.error("删除图片失败: id={}, url={}", image.getId(), image.getImageUrl());
            return false;
        }
    }
    
    /**
     * 批量删除商品所有图片
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAllProductImages(Long productId) {
        log.info("批量删除商品所有图片: productId={}", productId);
        
        if (productId == null) {
            log.error("参数无效: productId为空");
            return false;
        }
        
        // 查找该商品所有未删除的图片
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId)
               .eq(ProductImage::getDeleted, 0); // 只查询未删除的
        
        List<ProductImage> images = productImageMapper.selectList(wrapper);
        
        if (images.isEmpty()) {
            log.info("该商品没有图片需要删除: productId={}", productId);
            return true;
        }
        
        log.info("找到{}张需要删除的图片", images.size());
        
        // 批量逻辑删除图片
        for (ProductImage image : images) {
            image.setDeleted(1);
            image.setUpdatedTime(LocalDateTime.now());
            productImageMapper.updateById(image);
            log.info("图片已标记为删除: id={}, url={}", image.getId(), image.getImageUrl());
        }
        
        // 清空商品主图
        Product product = this.getById(productId);
        if (product != null) {
            product.setMainImage(null);
            product.setUpdatedTime(LocalDateTime.now());
            this.updateById(product);
            log.info("已清空商品主图: productId={}", productId);
        }
        
        return true;
    }
} 