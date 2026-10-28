package com.example.bdMetro.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ExpiryInterceptor expiryInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(ExpiryInterceptor expiryInterceptor,
                        RateLimitInterceptor rateLimitInterceptor) {
        this.expiryInterceptor = expiryInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
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

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/auth/**");
    }
}
