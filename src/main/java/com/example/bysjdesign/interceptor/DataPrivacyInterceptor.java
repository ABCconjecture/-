package com.example.bysjdesign.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

/**
 * 数据隐私保护拦截器
 * 记录敏感数据访问日志，防止数据泄露
 */
@Component
public class DataPrivacyInterceptor implements HandlerInterceptor {

    private static final Logger logger = Logger.getLogger(DataPrivacyInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录访问日志
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();

        logger.info("API访问 - 方法: " + method + ", URI: " + uri + ", IP: " + remoteAddr);

        // 对于敏感端点进行额外验证
        if (isSensitiveEndpoint(uri)) {
            String authorization = request.getHeader("Authorization");
            if (authorization == null || authorization.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        if (ex != null) {
            logger.warning("请求处理异常: " + ex.getMessage());
        }
    }

    /**
     * 检查是否为敏感端点
     */
    private boolean isSensitiveEndpoint(String uri) {
        String[] sensitiveEndpoints = {
                "/api/campus/profile",
                "/api/campus/warning",
                "/api/campus/analysis"
        };

        for (String endpoint : sensitiveEndpoints) {
            if (uri.contains(endpoint)) {
                return true;
            }
        }
        return false;
    }
}