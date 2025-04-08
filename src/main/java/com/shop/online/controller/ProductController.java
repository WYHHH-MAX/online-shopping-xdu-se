package com.shop.online.controller;

import com.shop.online.common.result.PageResult;
import com.shop.online.dto.ProductQueryDTO;
import com.shop.online.vo.ProductVO;
import com.shop.online.service.ProductService;
import com.shop.online.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Resource
    private ProductService productService;
    
    @Value("${spring.resources.static-locations:classpath:/static/}")
    private String staticResourceLocation;

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
    
    /**
     * 处理图片请求，当原始图片不存在时返回默认图片
     */
    @GetMapping("/placeholder")
    public ResponseEntity<byte[]> getPlaceholderImage() {
        try {
            // 尝试从classpath加载默认图片
            ClassPathResource resource = new ClassPathResource("static/images/placeholder.jpg");
            byte[] imageBytes;
            
            if (resource.exists()) {
                log.debug("使用默认placeholder图片");
                imageBytes = Files.readAllBytes(Paths.get(resource.getURI()));
            } else {
                log.warn("默认placeholder图片不存在，生成一个带文字的占位图");
                // 生成一个带"No Image"文字的占位图
                imageBytes = generatePlaceholderImage();
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
                
        } catch (IOException e) {
            log.error("获取默认图片失败", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 生成一个带"No Image"文字的占位图像
     * @return 图像字节数组
     */
    private byte[] generatePlaceholderImage() throws IOException {
        // 创建200x200的灰色图像
        int width = 200;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置图像背景为浅灰色
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);
        
        // 添加图像边框
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(0, 0, width - 1, height - 1);
        
        // 设置文字样式
        g2d.setColor(new Color(150, 150, 150));
        Font font = new Font("Arial", Font.BOLD, 24);
        g2d.setFont(font);
        
        // 添加"No Image"文字
        FontMetrics metrics = g2d.getFontMetrics(font);
        String text = "No Image";
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        g2d.drawString(text, (width - textWidth) / 2, height / 2);
        
        // 释放资源
        g2d.dispose();
        
        // 将图像转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
} 