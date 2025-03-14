package com.shop.online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.shop.online.mapper")
@EnableTransactionManagement
public class  OnlineShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineShopApplication.class, args);
    }
}