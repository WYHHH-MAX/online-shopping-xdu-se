package com.shop.online.controller;

import com.shop.online.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @GetMapping("/test")
    public Result<String> test() {
        logger.info("访问测试接口");
        return Result.success("服务器运行正常！");
    }

    @GetMapping("/health")
    public Result<String> health() {
        logger.info("访问健康检查接口");
        return Result.success("服务健康状态：正常");
    }

    @GetMapping("/version")
    public Result<String> version() {
        logger.info("访问版本信息接口");
        return Result.success("API版本：1.0.0");
    }
} 