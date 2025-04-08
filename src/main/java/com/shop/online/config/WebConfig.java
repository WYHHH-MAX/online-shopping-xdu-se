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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("配置CORS映射...");
        registry.addMapping("/**")
                // 不使用通配符，明确指定允许的源
                .allowedOrigins("http://localhost:5173", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization") // 允许前端访问的响应头
                .allowCredentials(true) // 保持true，但使用明确的源
                .maxAge(3600);
        logger.info("CORS映射配置完成: 明确允许的来源: [http://localhost:5173, http://localhost:8080]");
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
        
        // 不使用通配符，明确指定允许的源
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:8080");
        
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization"); // 允许前端访问的响应头
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        logger.info("CORS过滤器创建完成: 明确允许的来源: [http://localhost:5173, http://localhost:8080]");
        
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
        logger.info("配置静态资源处理器 - 开始");
        
        // 获取项目根目录，用于构建文件路径
        String projectRoot = System.getProperty("user.dir");
        String staticPath = projectRoot + "/src/main/resources/static/";
        logger.info("项目根目录: {}", projectRoot);
        logger.info("静态资源目录: {}", staticPath);
        
        // 添加上传文件资源处理 - 配置多个位置
        logger.info("配置静态资源处理器 - 添加uploads路径映射");
        
        // 配置上传文件资源处理器 - 禁用缓存，确保图片能正确刷新
        String uploadsPath = "file:" + projectRoot + "/uploads/";
        logger.info("上传文件实际路径: {}", uploadsPath);
        
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadsPath)
                .setCachePeriod(0)  // 禁用缓存
                .resourceChain(false);
        
        logger.info("上传文件资源映射配置完成: /api/uploads/** -> {}", uploadsPath);
        
        // 配置静态图片目录下的图片处理
        String imagesPath = "classpath:/static/images/";
        // 添加文件系统路径作为备用
        String fileSystemImagesPath = "file:" + projectRoot + "/src/main/resources/static/images/";
        logger.info("静态图片目录路径 (classpath): {}", imagesPath);
        logger.info("静态图片目录路径 (filesystem): {}", fileSystemImagesPath);
        
        // 添加强制响应头，确保图片正确显示
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations(imagesPath, fileSystemImagesPath)
                .setCachePeriod(0)  // 禁用缓存
                .resourceChain(false);
        
        // 同时支持不带/api前缀的图片路径访问
        registry.addResourceHandler("/images/**")
                .addResourceLocations(imagesPath, fileSystemImagesPath)
                .setCachePeriod(0)  // 禁用缓存
                .resourceChain(false);
        
        logger.info("静态图片资源映射配置完成: /api/images/** -> {}", imagesPath);
        logger.info("静态图片资源映射配置完成: /images/** -> {}", imagesPath);
        
        // 添加一个通用的静态资源处理，确保所有static目录资源可访问
        registry.addResourceHandler("/api/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0)
                .resourceChain(false);
        
        logger.info("通用静态资源映射配置完成: /api/static/** -> classpath:/static/");
        
        // 配置文件系统目录访问
        File staticDir = new File(staticPath);
        if (staticDir.exists() && staticDir.isDirectory()) {
            logger.info("静态资源目录存在: {}", staticPath);
            
            // 列出static目录下的子目录
            File[] subdirs = staticDir.listFiles(File::isDirectory);
            if (subdirs != null) {
                for (File dir : subdirs) {
                    logger.info("发现静态资源子目录: {}", dir.getName());
                }
            }
        } else {
            logger.warn("静态资源目录不存在或不是目录: {}", staticPath);
        }
        
        logger.info("静态资源处理器配置完成");
    }
}