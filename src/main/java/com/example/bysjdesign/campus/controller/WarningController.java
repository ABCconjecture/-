package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.RiskWarning;
import com.example.bysjdesign.repository.RiskWarningRepository;
import com.example.bysjdesign.service.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campus/warning")
public class WarningController {
    @Autowired private RiskWarningRepository riskWarningRepository;
    @Autowired private WarningService warningService;

    @GetMapping
    public Map<String, Object> getUserWarnings(@RequestParam(required = false) Integer userId) {
        List<RiskWarning> warnings = (userId != null)
                ? riskWarningRepository.findByUserIdOrderByCreateTimeDesc(userId)
                : riskWarningRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", warnings);
        return result;
    }

    @GetMapping("/stats")
    public Map<String, Object> getWarningStats() {
        List<RiskWarning> all = riskWarningRepository.findAll();
        // 统计各类型预警数量
        Map<String, Long> statsMap = all.stream().collect(Collectors.groupingBy(
                w -> w.getWarningType() != null ? w.getWarningType() : "综合风险", Collectors.counting()));
        Map<String, Object> data = new HashMap<>();
        data.put("types", new ArrayList<>(statsMap.keySet()));
        data.put("counts", new ArrayList<>(statsMap.values()));
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    @GetMapping("/high-risk")
    public Map<String, Object> getHighRiskUsers() {
        // 筛选未处理且高分值的风险记录
        List<RiskWarning> highRisk = riskWarningRepository.findAll().stream()
                .filter(w -> w.getStatus() == 0 && (w.getRiskScore() != null && w.getRiskScore() > 70))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", highRisk);
        return result;
    }

    @PostMapping("/trigger")
    public Map<String, Object> triggerWarningCheck() {
        Map<String, Object> result = new HashMap<>();
        try {
            warningService.checkAll(); // 调用业务层扫描日志
            result.put("code", 200);
            result.put("message", "预警检查任务已成功启动并执行");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "执行失败: " + (e.getMessage() != null ? e.getMessage() : "内部服务错误"));
        }
        return result;
    }
}