package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.AnalysisData;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.service.MultiDimensionalAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/campus/analysis")
public class AnalysisRestController {

    @Autowired private AnalysisDataRepository analysisDataRepository;
    @Autowired private CampusUserRepository userRepository;
    @Autowired private MultiDimensionalAnalysisService multiDimensionalAnalysisService;

    /**
     * ✅ 核心修复：支持按用户 ID 动态获取最新分析报告
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getUserAnalysis(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 优先从数据库查询已有数据
        List<AnalysisData> list = analysisDataRepository.findByUserId(userId, PageRequest.of(0, 1));

        if (list.isEmpty()) {
            // 2. 如果没有历史数据，尝试现场触发一次轻量级分析（防止新导入用户搜不到数据）
            try {
                AnalysisData freshData = multiDimensionalAnalysisService.analyzeUser(userId);
                result.put("code", 200);
                result.put("data", freshData);
                return result;
            } catch (Exception e) {
                result.put("code", 404);
                result.put("message", "未找到分析数据，且自动分析失败");
                return result;
            }
        }

        result.put("code", 200);
        result.put("data", list.get(0));
        return result;
    }

    /**
     * 获取历史分析记录
     */
    @GetMapping("/{userId}/history")
    public Map<String, Object> getHistory(@PathVariable Integer userId) {
        List<AnalysisData> list = analysisDataRepository.findByUserId(userId, PageRequest.of(0, 10));
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    /**
     * 手动触发指定用户的全量分析更新
     */
    @PostMapping("/{userId}/trigger")
    public Map<String, Object> triggerUserAnalysis(@PathVariable Integer userId) {
        try {
            AnalysisData data = multiDimensionalAnalysisService.analyzeUser(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "指标更新成功");
            result.put("data", data);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "分析失败: " + e.getMessage());
            return result;
        }
    }
}