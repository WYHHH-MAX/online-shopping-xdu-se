package com.shop.online.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("配置CORS映射...");
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 使用通配符允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization") // 允许前端访问的响应头
                .allowCredentials(true)
                .maxAge(3600);
        logger.info("CORS映射配置完成: 允许所有来源");
    }
    
    /**
     * 提供一个高优先级的CORS过滤器
     * 确保OPTIONS预检请求能被正确处理
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        logger.info("创建CORS过滤器...");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 允许所有来源
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization"); // 允许前端访问的响应头
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        logger.info("CORS过滤器创建完成: 允许所有来源");
        
        return new CorsFilter(source);
    }
    
    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/images/**", "/api/static/**", "/api/uploads/**");
        logger.info("请求日志拦截器配置完成");
    }
    
    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加上传文件资源处理 - 配置多个位置
        logger.info("配置静态资源处理器 - 添加uploads路径映射");
        
        // 配置上传文件资源处理器 - 禁用缓存，确保图片能正确刷新
        String uploadsPath = "file:D:/java/spm2/uploads/";
        logger.info("上传文件实际路径: {}", uploadsPath);
        
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadsPath)
                .setCachePeriod(0)  // 禁用缓存
                .resourceChain(false);
        
        logger.info("上传文件资源映射配置完成: /api/uploads/** -> {}", uploadsPath);
        
        // 不再需要验证旧的头像路径
        // 添加静态资源处理 - 禁用缓存以确保头像可以正确刷新
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(0)
                .resourceChain(false);
        
        logger.info("静态资源处理器配置完成");
    }
} 