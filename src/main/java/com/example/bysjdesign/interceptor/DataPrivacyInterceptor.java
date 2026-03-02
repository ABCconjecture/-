package com.example.bysjdesign.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@Component
public class DataPrivacyInterceptor implements HandlerInterceptor {
    private static final Logger logger = Logger.getLogger(DataPrivacyInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        // 降低日志级别，避免刷屏
        logger.fine("访问URI: " + uri);

        if (isSensitiveEndpoint(uri)) {
            String auth = request.getHeader("Authorization");
            if (auth == null || auth.isEmpty()) {
                // ✅ 仅记录但不拦截，确保首页统计数据（预警、画像）能正常加载
                logger.info("演示模式：放行未授权访问 -> " + uri);
                return true;
            }
        }
        return true;
    }

    private boolean isSensitiveEndpoint(String uri) {
        // 保持现状
        String[] sensitive = {"/api/campus/profile", "/api/campus/warning", "/api/campus/analysis"};
        for (String s : sensitive) {
            if (uri.contains(s)) return true;
        }
        return false;
    }
}