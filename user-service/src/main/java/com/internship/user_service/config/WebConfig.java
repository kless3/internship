package com.internship.user_service.config;

import com.internship.user_service.interceptor.JwtValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtValidationInterceptor jwtValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtValidationInterceptor)
                .addPathPatterns("/api/v1/users/**")
                .excludePathPatterns("/api/v1/users/*/exists")
                .excludePathPatterns("/api/v1/users")
                .excludePathPatterns("/api/v1/users/email/*")
                .excludePathPatterns("/api/v1/users/email/*/exists");
    }
}