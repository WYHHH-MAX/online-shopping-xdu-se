package com.shop.online.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品数据传输对象
 */
@Data
public class ProductDTO {
    /**
     * 商品ID（更新时需要）
     */
    private Long id;
    
    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空")
    private String name;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品价格
     */
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格不能小于0")
    private BigDecimal price;
    
    /**
     * 商品库存
     */
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;
    
    /**
     * 分类ID
     */
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;
    
    /**
     * 商品状态 0-下架 1-上架
     */
    private Integer status;
    
    /**
     * 是否推荐 0-否 1-是
     */
    private Integer isFeatured;
    
    /**
     * 主图URL
     */
    private String mainImage;
    
    /**
     * 图片URL列表
     */
    private List<String> images;
    
    /**
     * 卖家ID
     */
    private Long sellerId;
    
    /**
     * 卖家名称/店铺名称
     */
    private String sellerName;
    
    /**
     * 销售数量
     */
    private Integer salesCount;
    
    /**
     * 创建时间
     */
    private String createdTime;
    
    /**
     * 更新时间
     */
    private String updatedTime;
} 