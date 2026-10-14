package com.example.bdMetro.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ExpiryInterceptor expiryInterceptor;

    public WebMvcConfig(ExpiryInterceptor expiryInterceptor) {
        this.expiryInterceptor = expiryInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(expiryInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/auth/**",
                    "/ping",
                    "/api/payments/**"
                );
    }
}
