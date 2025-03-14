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
} 