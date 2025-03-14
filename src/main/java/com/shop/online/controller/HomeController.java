package com.shop.online.controller;

import com.shop.online.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Result<String> home() {
        return Result.success("Welcome to Online Shop API!");
    }
} 