package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.entity.User;
import com.shop.online.exception.BusinessException;
import com.shop.online.mapper.UserMapper;
import com.shop.online.service.UserService;
import com.shop.online.util.JwtUtil;
import com.shop.online.vo.auth.LoginRequest;
import com.shop.online.vo.auth.LoginResponse;
import com.shop.online.vo.auth.RegisterRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
                
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        // 生成token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
        
        // 构建返回对象
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setToken(token);
        
        return response;
    }

    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        checkUsernameUnique(request.getUsername());
        
        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(request, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 设置默认角色为买家
        user.setRole(0);
        user.setStatus(1);
        
        // 保存用户
        save(user);
    }

    @Override
    public User getCurrentUser() {
        throw new BusinessException("功能未实现");
    }

    @Override
    public void checkUsernameUnique(String username) {
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }
} 