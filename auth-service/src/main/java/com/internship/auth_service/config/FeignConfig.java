package com.internship.auth_service.config;

import com.internship.auth_service.interceptor.FeignJwtInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients
public class FeignConfig {

    @Bean
    public FeignJwtInterceptor feignJwtInterceptor() {
        return new FeignJwtInterceptor();
    }
}
