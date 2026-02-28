package com.example.bysjdesign.config;

import com.example.bysjdesign.interceptor.DataPrivacyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 * 注册数据隐私保护拦截器和静态资源处理
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DataPrivacyInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login", "/api/health");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 配置 index.html
        registry.addResourceHandler("/")
                .addResourceLocations("classpath:/static/index.html");
    }
}