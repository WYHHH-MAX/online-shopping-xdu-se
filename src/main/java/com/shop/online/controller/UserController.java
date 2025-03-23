package com.shop.online.controller;

import com.shop.online.entity.User;
import com.shop.online.service.UserService;
import com.shop.online.util.FileUtil;
import com.shop.online.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    /**
     * 更新用户个人资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> params) {
        log.info("更新用户个人资料，参数: {}", params);
        String nickname = params.get("nickname");
        String phone = params.get("phone");
        String email = params.get("email");
        
        try {
            userService.updateProfile(nickname, phone, email);
            return Result.success();
        } catch (Exception e) {
            log.error("更新用户个人资料失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 上传用户头像
     * 
     * @param file 头像文件
     * @return 头像路径
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传用户头像，文件名: {}, 大小: {}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 上传文件并获取路径
            String avatarPath = FileUtil.uploadUserAvatar(file, currentUser.getId());
            
            // 更新用户头像
            userService.updateAvatar(avatarPath);
            
            // 返回完整URL
            String fullUrl = avatarPath;
            log.info("头像上传成功，路径: {}", fullUrl);
            
            return Result.success(fullUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        log.info("获取当前用户信息");
        
        try {
            User user = userService.getCurrentUser();
            
            if (user == null) {
                return Result.error("获取用户信息失败");
            }
            
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }
} 