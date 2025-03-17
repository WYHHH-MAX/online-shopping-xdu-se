package com.shop.online.service;

import com.shop.online.entity.User;
import com.shop.online.vo.auth.LoginRequest;
import com.shop.online.vo.auth.LoginResponse;
import com.shop.online.vo.auth.RegisterRequest;

public interface UserService {
    LoginResponse login(LoginRequest request);
    
    void register(RegisterRequest request);
    
    User getCurrentUser();
    
    void checkUsernameUnique(String username);
    
    /**
     * 根据用户ID获取用户信息
     */
    User getUserById(Long userId);
} 