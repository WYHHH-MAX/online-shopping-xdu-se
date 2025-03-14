package com.shop.online.vo.auth;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String token;
    private Integer role;
} 