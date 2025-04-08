package com.shop.online.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.online.entity.User;
import com.shop.online.vo.auth.LoginRequest;
import com.shop.online.vo.auth.LoginResponse;
import com.shop.online.vo.auth.RegisterRequest;

import java.util.List;

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
    
    /**
     * 根据条件查询用户列表
     * @param queryWrapper 查询条件
     * @return 用户列表
     */
    List<User> list(LambdaQueryWrapper<User> queryWrapper);
} 