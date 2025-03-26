package com.shop.online.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 测试控制器
 * 用于提供测试端点，帮助排查问题
 */
@RestController
@RequestMapping("/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    /**
     * 测试服务器是否在线
     */
    @GetMapping("/ping")
    public String ping() {
        logger.info("收到ping请求");
        return "pong";
    }
    
    /**
     * 直接提供头像文件的端点
     * 通过直接返回文件内容，绕过资源处理器
     */
    @GetMapping("/avatar/{userId}")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long userId) {
        logger.info("直接请求用户头像: {}", userId);
        
        // 构建头像文件路径
        String avatarFileName = "user_" + userId + ".png";
        String avatarPath = "D:/java/spm2/uploads/avatars/" + avatarFileName;
        
        // 检查文件是否存在
        File avatarFile = new File(avatarPath);
        if (!avatarFile.exists()) {
            logger.error("头像文件不存在: {}", avatarPath);
            return ResponseEntity.notFound().build();
        }
        
        logger.info("找到头像文件: {}, 大小: {}字节", avatarPath, avatarFile.length());
        
        // 创建资源对象
        Resource resource = new FileSystemResource(avatarFile);
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + avatarFileName + "\"");
        headers.add("Access-Control-Allow-Origin", "*"); // 添加CORS头
        
        // 设置内容类型
        String contentType = "image/png";
        try {
            contentType = Files.probeContentType(Paths.get(avatarPath));
            if (contentType == null) {
                contentType = "image/png"; // 默认为PNG
            }
        } catch (IOException e) {
            logger.warn("无法确定文件类型，使用默认类型: image/png", e);
        }
        
        logger.info("返回头像文件, 内容类型: {}", contentType);
        
        // 构建响应
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
} 