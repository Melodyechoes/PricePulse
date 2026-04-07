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
                        "/api/auth/**",
                        "/api/products/**",
                        "/api/user-products/**",
                        "/api/notifications/**",
                        "/api/dashboard/**",
                        "/api/pdd/**",
                        "/api/admin/**",  // 排除所有管理员接口（使用 JWT 认证）
                        "/error",
                        "/static/**",
                        "/api/test/**",
                        "/webjars/**",
                        "/favicon.ico"
                );

        // JWT 拦截器（新版，用于 Token 认证）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/auth/change-password", "/api/admin/**")  // 需要 Token 保护的路径
                .excludePathPatterns(
                        "/error"
                );
    }
}
