package com.shop.online.controller;

import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.service.ProductService;
import com.shop.online.vo.ProductVO;
import com.shop.online.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);
    
    @Autowired
    private ProductService productService;

    @GetMapping("/test")
    public Result<String> test() {
//        logger.info("访问测试接口");
        return Result.success("服务器运行正常！");
    }

    @GetMapping("/health")
    public Result<String> health() {
//        logger.info("访问健康检查接口");
        return Result.success("服务健康状态：正常");
    }

    @GetMapping("/version")
    public Result<String> version() {
//        logger.info("访问版本信息接口");
        return Result.success("API版本：1.0.0");
    }
    
    /**
     * 搜索商品接口
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @param sortBy 排序方式
     * @return 商品列表
     */
    @GetMapping("/search")
    public Result<PageResult<ProductVO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String sortBy) {
//        logger.info("搜索商品: keyword={}, page={}, size={}, sortBy={}", keyword, page, size, sortBy);
        
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        
        // 设置排序
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc":
                    queryDTO.setOrderBy("price");
                    queryDTO.setIsAsc(true);
                    break;
                case "price_desc":
                    queryDTO.setOrderBy("price");
                    queryDTO.setIsAsc(false);
                    break;
                case "sales_desc":
                    queryDTO.setOrderBy("sales");
                    queryDTO.setIsAsc(false);
                    break;
                default:
                    // 默认按更新时间降序
                    queryDTO.setOrderBy("created_time");
                    queryDTO.setIsAsc(false);
                    break;
            }
        }
        
        PageResult<ProductVO> result = productService.getProductsByCondition(queryDTO);
//        logger.info("搜索结果: total={}", result.getTotal());
        
        return Result.success(result);
    }
} 