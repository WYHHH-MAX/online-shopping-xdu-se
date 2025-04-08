package com.shop.online.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Lazy  // 添加懒加载注解，打破循环依赖
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 设置不经过Spring Security的请求路径
        web.ignoring()
           .antMatchers("/api/images/**", "/api/static/**", "/api/uploads/**","/images/**")
           .antMatchers(HttpMethod.OPTIONS, "/**"); // 允许所有OPTIONS请求通过
        
        // 使用自定义的HttpFirewall，更宽松地处理特殊字符
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护
            .csrf().disable()
            // 配置跨域 - 基础配置，实际由WebConfig处理
            .cors().and()
            // 使用无状态会话
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            // 配置请求授权
            .authorizeRequests()
                // 登录相关接口
                .antMatchers("/api/auth/**").permitAll()
                // 商品查询接口
                .antMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                // 订单相关API
                .antMatchers("/api/orders/**").permitAll()
                // 其他请求
                .anyRequest().permitAll()
            .and()
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 配置更宽松的HTTP防火墙
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        return firewall;
    }
} 