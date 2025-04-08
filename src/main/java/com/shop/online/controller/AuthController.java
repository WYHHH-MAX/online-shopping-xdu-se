package com.shop.online.controller;

import com.shop.online.dto.order.PayOrderDTO;
import com.shop.online.service.UserService;
import com.shop.online.vo.Result;
import com.shop.online.vo.auth.LoginRequest;
import com.shop.online.vo.auth.LoginResponse;
import com.shop.online.vo.auth.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }
    
    /**
     * 验证当前token是否有效
     * @return 验证结果
     */
    @GetMapping("/check-token")
    public Result<Boolean> checkToken() {
        // 如果能通过拦截器（JWT检查）到达这个接口，说明token是有效的
        return Result.success(true);
    }
} 