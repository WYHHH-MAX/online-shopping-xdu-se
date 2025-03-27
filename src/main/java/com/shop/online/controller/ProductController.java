package com.shop.online.controller;

import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.vo.ProductVO;
import com.shop.online.service.ProductService;
import com.shop.online.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Resource
    private ProductService productService;

    @GetMapping("/category/{categoryId}")
    public Result getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String sortBy) {
        log.info("根据分类查询商品: categoryId={}, page={}, size={}, sortBy={}", categoryId, page, size, sortBy);
        return Result.success(productService.getProductsByCategory(categoryId, page, size, sortBy));
    }

    @GetMapping("/{id}")
    public ProductVO getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @GetMapping("/search")
    public PageResult<ProductVO> searchProducts(@ModelAttribute ProductQueryDTO queryDTO) {
        return productService.getProductsByCondition(queryDTO);
    }

    @GetMapping("/seller/{sellerId}")
    public PageResult<ProductVO> getProductsBySeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return productService.getProductsBySeller(sellerId, page, size);
    }

    @GetMapping("/featured")
    public PageResult<ProductVO> getFeaturedProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return productService.getFeaturedProducts(page, size);
    }
} 