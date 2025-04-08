package com.shop.online.service;

import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.entity.Product;
import com.shop.online.vo.ProductVO;
import java.util.List;
import java.util.Map;

public interface ProductService {
    /**
     * 根据分类ID获取商品列表
     */
    PageResult<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size);
    
    /**
     * 根据条件查询商品
     */
    PageResult<ProductVO> getProductsByCondition(ProductQueryDTO queryDTO);
    
    /**
     * 获取商品详情
     */
    ProductVO getProductDetail(Long id);
    
    /**
     * 获取卖家的商品列表
     */
    PageResult<ProductVO> getProductsBySeller(Long sellerId, Integer page, Integer size);

    /**
     * 获取推荐商品列表
     */
    PageResult<ProductVO> getFeaturedProducts(Integer page, Integer size);

    /**
     * 根据分类ID获取商品列表(包含排序)
     */
    PageResult<ProductVO> getProductsByCategoryWithSort(Long categoryId, Integer page, Integer size, String sortBy);

    /**
     * 根据分类ID获取商品列表(分类ID为Integer类型)
     */
    PageResult<ProductVO> getProductsByCategory(Integer categoryId, Integer page, Integer size, String sortBy);

    /**
     * 创建商品
     * @param product 商品信息
     * @return 商品详情
     */
    ProductVO saveProduct(Product product);

    /**
     * 更新商品
     * @param product 商品信息
     * @return 商品详情
     */
    ProductVO updateProduct(Product product);

    /**
     * 更新商品库存
     * @param productId 商品ID
     * @param stock 库存数量
     * @return 是否成功
     */
    boolean updateStock(Long productId, Integer stock);

    /**
     * 批量更新商品库存
     * @param sellerId 卖家ID
     * @param stockMap 商品ID与库存映射
     * @return 是否成功
     */
    boolean batchUpdateStock(Long sellerId, Map<Long, Integer> stockMap);

    /**
     * 统计卖家商品总数
     * @param sellerId 卖家ID
     * @return 商品总数
     */
    int countSellerProducts(Long sellerId);

    /**
     * 统计卖家库存不足的商品数量
     * @param sellerId 卖家ID
     * @param threshold 库存阈值
     * @return 库存不足的商品数量
     */
    int countSellerLowStockProducts(Long sellerId, Integer threshold);

    /**
     * 删除商品（逻辑删除）
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long productId);

    /**
     * 获取商品的图片数量
     * @param productId 商品ID
     * @return 图片数量
     */
    int getProductImageCount(Long productId);

    /**
     * 验证商品是否属于指定卖家
     * @param productId 商品ID
     * @param sellerId 卖家ID
     * @return 是否属于该卖家
     */
    boolean isProductOwnedBySeller(Long productId, Long sellerId);

    /**
     * 删除商品图片(根据URL)
     * @param productId 商品ID
     * @param imageUrl 图片URL
     * @return 操作结果
     */
    boolean deleteProductImage(Long productId, String imageUrl);

    /**
     * 批量删除商品所有图片
     * @param productId 商品ID
     * @return 操作结果
     */
    boolean deleteAllProductImages(Long productId);
} 