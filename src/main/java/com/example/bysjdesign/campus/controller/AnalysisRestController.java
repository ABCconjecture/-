package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.AnalysisData;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import com.example.bysjdesign.service.MultiDimensionalAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析API控制器
 * 提供用户分析数据、趋势分析等REST接口
 */
@RestController
@RequestMapping("/api/campus/analysis")
@CrossOrigin(origins = "http://localhost:3000")
public class AnalysisRestController {

    @Autowired
    private AnalysisDataRepository analysisDataRepository;

    @Autowired
    private MultiDimensionalAnalysisService multiDimensionalAnalysisService;

    /**
     * 获取用户的最新分析数据
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getUserAnalysis(@PathVariable Integer userId) {
        Pageable pageable = PageRequest.of(0, 1);
        List<AnalysisData> analysisData = analysisDataRepository.findByUserId(userId, pageable);

        Map<String, Object> result = new HashMap<>();

        if (analysisData.isEmpty()) {
            result.put("code", 404);
            result.put("message", "未找到分析数据");
            return result;
        }

        result.put("code", 200);
        result.put("message", "成功获取分析数据");
        result.put("data", analysisData.get(0));

        return result;
    }

    /**
     * 获取用户的分析历史记录
     */
    @GetMapping("/{userId}/history")
    public Map<String, Object> getAnalysisHistory(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<AnalysisData> analysisData = analysisDataRepository.findByUserId(userId, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取分析历史");
        result.put("data", analysisData);
        result.put("page", page);
        result.put("size", size);

        return result;
    }

    /**
     * 获取健康度趋势
     */
    @GetMapping("/trend/health")
    public Map<String, Object> getTrendData() {
        // 获取最近30天的分析数据用于绘制趋势图
        List<AnalysisData> recentData = analysisDataRepository.findAll();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取趋势数据");
        result.put("data", recentData);

        return result;
    }

    /**
     * 获取所有分析统计
     */
    @GetMapping("/stats")
    public Map<String, Object> getAnalysisStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAnalysis", analysisDataRepository.count());
        stats.put("highRiskCount", analysisDataRepository.findAll().stream()
                .filter(a -> a.getRiskScore() > 70)
                .count());
        stats.put("avgHealthScore", analysisDataRepository.findAll().stream()
                .mapToDouble(a -> 100 - a.getRiskScore())
                .average()
                .orElse(0.0));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取统计数据");
        result.put("data", stats);

        return result;
    }

    /**
     * 手动触发用户分析更新
     */
    @PostMapping("/{userId}/trigger")
    public Map<String, Object> triggerUserAnalysis(@PathVariable Integer userId) {
        try {
            multiDimensionalAnalysisService.analyzeAllUsers();

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "分析任务已触发");

            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "分析失败: " + e.getMessage());

            return result;
        }
    }
}
