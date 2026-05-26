package com.campusmarket.config;

import com.campusmarket.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 权限配置说明：
     * - permitAll：登录、注册、验证码、文件访问、商品浏览/搜索/详情
     * - authenticated：购物车、订单、钱包、评价（需登录即可）
     * - hasRole("MERCHANT")：商家专用接口（发布/下架商品、管理订单）
     * - hasRole("ADMIN")：管理员专用接口（审核用户/商品、充值、删除）
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 公开接口：登录、注册、验证码、上传文件、商品浏览
            .antMatchers("/auth/**", "/captcha/**", "/uploads/**").permitAll()
            .antMatchers("/products", "/products/**", "/categories/**").permitAll()
            .antMatchers("/reviews/merchant/**", "/user/info/**").permitAll()
            // 文件上传（注册时也需要用，故放开）
            .antMatchers("/files/upload").permitAll()
            // 管理员专用
            .antMatchers("/admin/**").hasRole("ADMIN")
            // 个人资料需登录
            .antMatchers("/user/**").authenticated()
            // 商家专用
            .antMatchers("/products/merchant/**").hasRole("MERCHANT")
            // 登录即可访问
            .antMatchers("/cart/**", "/orders/**", "/wallet/**", "/reviews/**").authenticated()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
