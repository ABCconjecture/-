package com.example.bysjdesign.config;

import com.example.bysjdesign.interceptor.DataPrivacyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ✅ 仅拦截 API 请求，排除静态资源干扰
        registry.addInterceptor(new DataPrivacyInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login", "/api/health");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 核心修复：确保 .js/ .css 文件的 MIME 类型被正确识别，不再显示源码
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}