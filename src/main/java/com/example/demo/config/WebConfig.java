package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;
    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. Logging Interceptor - chạy TRƯỚC để log request đầu tiên
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .order(1);  // Chạy đầu tiên

        registry.addInterceptor(getPermissionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/refresh",
                        "/users/registration",
                        "/v1/admin/ingredient/**",
                        "/v1/admin/admin_user/create",
                        "/api/v1/**",
                        "/error",
                "/v1/admin/restaurant-user/login",
                "/auth/logout"
                )
                .order(2);

    }

}
