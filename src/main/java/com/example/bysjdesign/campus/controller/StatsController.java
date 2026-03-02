package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.repository.WarningLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计API控制器
 * 提供系统统计数据、趋势分析等REST接口
 */
@RestController
@RequestMapping("/api/campus")
// ✅ 已移除冗余的 @CrossOrigin(origins = "http://localhost:3000")
// 跨域规则现在由全局 CorsConfig.java 统一管理，支持凭证携带且不会报错。
public class StatsController {

    @Autowired
    private CampusUserRepository userRepository;

    @Autowired
    private WarningLogRepository warningLogRepository;

    @Autowired
    private AnalysisDataRepository analysisDataRepository;

    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        stats.put("totalUsers", userRepository.count());
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == 1)
                .count();
        stats.put("activeUsers", activeUsers);

        // 预警统计
        stats.put("unhandledWarnings", warningLogRepository.countByStatus(0));
        stats.put("totalWarnings", warningLogRepository.count());

        // 分析统计
        long highRiskUsers = analysisDataRepository.findAll().stream()
                .filter(a -> a.getRiskScore() > 70)
                .count();
        stats.put("highRiskUsers", highRiskUsers);

        // 健康度统计
        double avgHealth = analysisDataRepository.findAll().stream()
                .mapToDouble(a -> 100 - a.getRiskScore())
                .average()
                .orElse(75.0);
        stats.put("avgHealthScore", Math.round(avgHealth * 100.0) / 100.0);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取系统统计");
        result.put("data", stats);

        return result;
    }

    /**
     * 获取健康度趋势数据
     */
    @GetMapping("/trend")
    public Map<String, Object> getTrendData() {
        List<Map<String, Object>> trendData = analysisDataRepository.findAll().stream()
                .map(a -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", a.getAnalysisDate());
                    item.put("healthScore", 100 - a.getRiskScore());
                    item.put("riskScore", a.getRiskScore());
                    return item;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取趋势数据");
        result.put("data", trendData);

        return result;
    }

    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "系统正常");
        result.put("status", "healthy");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 获取系统概览信息
     */
    @GetMapping("/overview")
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        long totalUsers = userRepository.count();
        overview.put("totalUsers", totalUsers);

        long maleCount = userRepository.findAll().stream()
                .filter(u -> "男".equals(u.getGender()))
                .count();
        overview.put("males", maleCount);
        overview.put("females", totalUsers - maleCount);

        List<Object[]> collegeDistribution = userRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        CampusUser::getCollege,
                        java.util.stream.Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toList();
        overview.put("collegeDistribution", collegeDistribution);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取系统概览");
        result.put("data", overview);

        return result;
    }
}