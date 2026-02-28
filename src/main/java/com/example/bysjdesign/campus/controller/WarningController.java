package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.RiskWarning;
import com.example.bysjdesign.campus.entity.WarningLog;
import com.example.bysjdesign.repository.RiskWarningRepository;
import com.example.bysjdesign.repository.WarningLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 预警API控制器
 * 提供预警查询、处理等REST接口
 */
@RestController
@RequestMapping("/api/campus/warning")
@CrossOrigin(origins = "http://localhost:3000")
public class WarningController {

    @Autowired
    private WarningLogRepository warningLogRepository;

    @Autowired
    private RiskWarningRepository riskWarningRepository;

    /**
     * 获取预警统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getWarningStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("unhandledCount", warningLogRepository.countByStatus(0));
        stats.put("handledCount", warningLogRepository.countByStatus(1));
        stats.put("totalCount", warningLogRepository.count());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取预警统计");
        result.put("data", stats);

        return result;
    }

    /**
     * 获取未处理的预警列表
     */
    @GetMapping("/unhandled")
    public Map<String, Object> getUnhandledWarnings() {
        List<WarningLog> warnings = warningLogRepository.findByStatus(0);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取未处理预警");
        result.put("data", warnings);
        result.put("count", warnings.size());

        return result;
    }

    /**
     * 获取高风险用户列表
     */
    @GetMapping("/high-risk")
    public Map<String, Object> getHighRiskUsers() {
        List<RiskWarning> highRiskWarnings = riskWarningRepository.findByStatusOrderByCreateTimeDesc(0);
        List<Integer> highRiskUserIds = highRiskWarnings.stream()
                .map(RiskWarning::getUserId)
                .distinct()
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取高风险用户列表");
        result.put("data", highRiskUserIds);
        result.put("count", highRiskUserIds.size());

        return result;
    }

    /**
     * 获取用户的预警信息
     */
    @GetMapping
    public Map<String, Object> getUserWarnings(@RequestParam Integer userId) {
        List<RiskWarning> userWarnings = riskWarningRepository.findByUserIdOrderByCreateTimeDesc(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取用户预警");
        result.put("data", userWarnings);
        result.put("count", userWarnings.size());

        return result;
    }

    /**
     * 处理预警
     */
    @PostMapping("/handle/{warningId}")
    public Map<String, Object> handleWarning(
            @PathVariable Integer warningId,
            @RequestBody Map<String, String> request) {

        Optional<RiskWarning> warning = riskWarningRepository.findById(warningId);

        if (warning.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "预警不存在");

            return result;
        }

        RiskWarning w = warning.get();
        w.setStatus(2);  // 2: 已解决
        w.setHandlerRemark(request.getOrDefault("remark", ""));
        riskWarningRepository.save(w);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "预警已处理");
        result.put("data", w);

        return result;
    }

    /**
     * 获取预警分布（按类型）
     */
    @GetMapping("/distribution/type")
    public Map<String, Object> getWarningDistributionByType() {
        List<Object[]> typeDistribution = riskWarningRepository.countByType();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取预警分布");
        result.put("data", typeDistribution);

        return result;
    }

    /**
     * 获取预警分布（按级别）
     */
    @GetMapping("/distribution/level")
    public Map<String, Object> getWarningDistributionByLevel() {
        List<Object[]> levelDistribution = riskWarningRepository.countByLevelUnhandled();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取预警级别分布");
        result.put("data", levelDistribution);

        return result;
    }
}
