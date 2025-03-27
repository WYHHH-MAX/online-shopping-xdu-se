package com.shop.online.config;

import com.shop.online.entity.User;
import com.shop.online.mapper.UserMapper;
import com.shop.online.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;  // 直接使用UserMapper而不是UserService

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 静态资源不处理
        if (path.contains("/api/images/") || path.contains("/api/static/") || path.contains("/api/uploads/")) {
            return true;
        }
        // 打印请求路径和方法
//        logger.info("JWT过滤器处理请求: {} {}", request.getMethod(), path);
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
//        logger.info("===== JWT过滤器开始处理请求: {} {} =====", request.getMethod(), requestURI);
        
        // 打印所有请求头
//        logger.info("请求头信息:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
//            logger.info("{}: {}", headerName, request.getHeader(headerName));
        }
        
        try {
            String authHeader = request.getHeader("Authorization");
//            logger.info("Authorization头: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // 只输出token的前10个字符，避免日志中包含完整token
//                logger.info("JWT令牌: {}...", token.substring(0, Math.min(10, token.length())));
                
                if (jwtUtil.validateToken(token)) {
//                    logger.info("JWT令牌有效");
                    Long userId = jwtUtil.extractUserId(token);
//                    logger.info("从JWT令牌中提取的用户ID: {}", userId);
                    
                    if (userId != null) {
                        // 直接使用userMapper获取用户
                        User user = userMapper.selectById(userId);
//                        logger.info("根据用户ID查询用户结果: {}", user != null ? user.getUsername() : "null");
                        
                        if (user != null) {
                            // 设置认证信息到Spring Security上下文
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                            
                            SecurityContextHolder.getContext().setAuthentication(authToken);
//                            logger.info("已成功设置用户认证信息到上下文: {}", user.getUsername());
                        } else {
                            logger.warn("未能找到用户ID为{}的用户", userId);
                        }
                    } else {
                        logger.warn("从JWT令牌中未能提取到用户ID");
                    }
                } else {
                    logger.warn("JWT令牌无效");
                }
            } else {
                logger.info("请求中没有JWT令牌或格式不正确");
            }
        } catch (Exception e) {
            logger.error("处理JWT认证时发生错误", e);
            SecurityContextHolder.clearContext();
        }

        logger.info("JWT过滤器处理完毕，即将调用下一个过滤器");
        try {
            filterChain.doFilter(request, response);
            logger.info("===== 过滤器链执行完毕，返回处理 =====");
        } catch (Exception e) {
            logger.error("过滤器链执行出错", e);
            throw e;
        }
    }
} 