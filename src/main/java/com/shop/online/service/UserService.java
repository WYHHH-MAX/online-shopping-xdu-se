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
    
    /**
     * 更新用户个人资料
     */
    void updateProfile(String nickname, String phone, String email);
    
    /**
     * 更新用户头像
     */
    void updateAvatar(String avatarPath);
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 角色值：0-买家，1-卖家，2-管理员
     * @return 是否更新成功
     */
    boolean updateUserRole(Long userId, Integer role);
} 