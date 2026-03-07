package com.pricepulse.backend.common.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器（旧版，用于 Session）
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/login/**",
                        "/api/register/**",
                        "/api/auth/**",  // 排除认证接口
                        "/api/products/**",
                        "/api/user-products/**",
                        "/api/notifications/**",  // 新增：排除通知接口
                        "/error",
                        "/static/**",
                        "/webjars/**"
                );

        // JWT 拦截器（新版，用于 Token 认证）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/protected/**")  // 需要 Token 保护的路径
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/products/**",
                        "/api/notifications/**",  // 新增：排除通知接口
                        "/error"
                );
    }
}
