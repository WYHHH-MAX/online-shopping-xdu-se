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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

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
//        logger.info("尝试获取当前登录用户");
        
        // 首先尝试从Spring Security上下文中获取
        try {
//            logger.info("从Spring Security上下文中获取用户...");
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//                logger.info("获取到SecurityContext中的principal: {}", principal);
                
                if (principal instanceof UserDetails) {
                    String username = ((UserDetails) principal).getUsername();
//                    logger.info("从principal中获取到用户名: {}", username);
                    User user = getOne(new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, username));
                    if (user != null) {
//                        logger.info("从数据库中找到了用户: {}", username);
                        return user;
                    }
                } else if (principal instanceof String) {
                    // 有时候principal可能只是一个字符串用户名
                    String username = (String) principal;
//                    logger.info("principal是一个字符串: {}", username);
                    if (!"anonymousUser".equals(username)) {
                        User user = getOne(new LambdaQueryWrapper<User>()
                                .eq(User::getUsername, username));
                        if (user != null) {
//                            logger.info("从数据库中找到了用户: {}", username);
                            return user;
                        }
                    }
                }
            } else {
                logger.info("SecurityContext.getAuthentication() 为 null");
            }
        } catch (Exception e) {
            logger.error("从SecurityContext获取用户失败", e);
        }
        
        // 如果上下文中没有，尝试从请求头中获取JWT
        try {
//            logger.info("尝试从请求头中获取JWT...");
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
//                logger.info("获取到的Authorization头: {}", authHeader);
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
//                    logger.info("解析得到的JWT令牌: {}", token);
                    
                    if (jwtUtil.validateToken(token)) {
//                        logger.info("JWT令牌有效");
                        Long userId = jwtUtil.extractUserId(token);
//                        logger.info("从JWT中提取的用户ID: {}", userId);
                        
                        if (userId != null) {
                            User user = getById(userId);
                            if (user != null) {
//                                logger.info("成功从JWT中解析得到用户: {}", user.getUsername());
                                return user;
                            } else {
                                logger.warn("在数据库中未找到用户ID为{}的用户", userId);
                            }
                        }
                    } else {
                        logger.warn("JWT令牌无效");
                    }
                }
            } else {
                logger.warn("无法获取当前请求上下文");
            }
        } catch (Exception e) {
            logger.error("从JWT获取用户失败", e);
        }
        
        logger.warn("未能找到当前登录用户");
        return null;
    }

    @Override
    public void checkUsernameUnique(String username) {
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        return getById(userId);
    }

    /**
     * 更新用户个人资料
     */
    @Override
    public void updateProfile(String nickname, String phone, String email) {
        // 获取当前用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 更新用户信息
        User user = new User();
        user.setId(currentUser.getId());
        
        if (nickname != null) {
            user.setNickname(nickname);
        }
        
        if (phone != null) {
            user.setPhone(phone);
        }
        
        if (email != null) {
            user.setEmail(email);
        }
        
        user.setUpdatedTime(LocalDateTime.now());
        
        // 更新数据库
        updateById(user);
        
//        logger.info("用户个人资料已更新，用户ID: {}", currentUser.getId());
    }

    /**
     * 更新用户头像
     */
    @Override
    public void updateAvatar(String avatarPath) {
        // 获取当前用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 获取用户原来的头像路径
        String oldAvatarPath = currentUser.getAvatar();
        
        // 更新用户头像
        User user = new User();
        user.setId(currentUser.getId());
        user.setAvatar(avatarPath);
        user.setUpdatedTime(LocalDateTime.now());
        
        // 更新数据库
        updateById(user);
        
//        logger.info("用户头像已更新，用户ID: {}, 旧头像路径: {}, 新头像路径: {}",
//                    currentUser.getId(), oldAvatarPath, avatarPath);
    }

    @Override
    public boolean updateUserRole(Long userId, Integer role) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新角色
        user.setRole(role);
        
        return updateById(user);
    }
    
    /**
     * 根据条件查询用户列表
     */
    @Override
    public List<User> list(LambdaQueryWrapper<User> queryWrapper) {
        return baseMapper.selectList(queryWrapper);
    }
} 