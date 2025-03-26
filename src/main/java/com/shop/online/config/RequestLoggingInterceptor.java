package com.shop.online.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求日志拦截器
 * 用于跟踪请求的整个处理流程
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getRequestURI().contains("/api/images/") && 
            !request.getRequestURI().contains("/api/static/") && 
            !request.getRequestURI().contains("/api/uploads/")) {
            logger.info("===== 请求开始处理: {} {} =====", request.getMethod(), request.getRequestURI());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!request.getRequestURI().contains("/api/images/") && 
            !request.getRequestURI().contains("/api/static/") && 
            !request.getRequestURI().contains("/api/uploads/")) {
            logger.info("===== 请求处理完成: {} {} - 状态码: {} =====", 
                    request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            logger.error("===== 请求处理异常: {} {} - 异常: {} =====", 
                    request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        }
    }
} 