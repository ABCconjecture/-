package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.*;
import com.example.bysjdesign.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * 多维度分析服务
 */
@Service
public class MultiDimensionalAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(MultiDimensionalAnalysisService.class);

    // ==================== 依赖注入 ====================

    @Autowired
    private AnalysisDataRepository analysisDataRepository;

    @Autowired
    private WarningLogRepository warningLogRepository;

    @Autowired
    private RiskWarningRepository riskWarningRepository;

    @Autowired
    private NetworkLogRepository networkLogRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private BorrowLogRepository borrowLogRepository;

    @Autowired
    private CampusUserRepository campusUserRepository;

    // ==================== 常量定义 ====================

    private static final double HIGH_RISK_THRESHOLD = 70.0;
    private static final double MEDIUM_RISK_THRESHOLD = 50.0;
    private static final int ANALYSIS_DAYS = 30;

    // ==================== 核心分析方法 ====================

    public void analyzeAllUsers() {
        logger.info("========== 开始分析所有用户 ==========");
        long startTime = System.currentTimeMillis();

        try {
            List<CampusUser> allUsers = campusUserRepository.findAll();
            int successCount = 0;
            int failCount = 0;

            for (CampusUser user : allUsers) {
                try {
                    analyzeUser(user.getId());
                    successCount++;
                } catch (Exception e) {
                    logger.error("分析用户 {} 失败", user.getId(), e);
                    failCount++;
                }
            }
            long duration = System.currentTimeMillis() - startTime;
            logger.info("========== 用户分析完成，耗时: {}ms ==========", duration);
        } catch (Exception e) {
            logger.error("分析所有用户异常", e);
            throw new RuntimeException("分析失败", e);
        }
    }

    public AnalysisData analyzeUser(Long userId) {
        logger.debug("开始分析用户 {}", userId);
        try {
            Map<String, Object> networkData = analyzeNetworkData(userId);
            Map<String, Object> accessData = analyzeAccessData(userId);
            Map<String, Object> borrowData = analyzeBorrowData(userId);

            double riskScore = calculateRiskScore(networkData, accessData, borrowData);
            AnalysisData analysisData = createAnalysisData(userId, riskScore, networkData, accessData, borrowData);

            AnalysisData savedData = analysisDataRepository.save(analysisData);
            generateOrUpdateWarning(userId, riskScore);

            return savedData;
        } catch (Exception e) {
            logger.error("分析用户 {} 异常", userId, e);
            throw new RuntimeException("用户分析失败", e);
        }
    }

    // ==================== 数据收集方法 ====================

    private Map<String, Object> analyzeNetworkData(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(ANALYSIS_DAYS);
            // ✅ 已修正：Repository 现在接收 Long，直接透传 userId
            List<NetworkLog> logs = networkLogRepository.findByUserIdAndSessionStartAfter(userId, startTime);

            if (logs.isEmpty()) {
                initializeDefaultNetworkData(result);
                return result;
            }

            long totalMinutes = logs.stream()
                    .mapToLong(log -> (log.getSessionStart() != null && log.getSessionEnd() != null) ?
                            (log.getSessionEnd().getTime() - log.getSessionStart().getTime()) / (1000 * 60) : 0)
                    .sum();
            double avgOnlineHours = (double) totalMinutes / ANALYSIS_DAYS / 60.0;
            result.put("avgOnlineHours", avgOnlineHours);
            result.put("isAbnormal", avgOnlineHours > 10);
            result.put("studyTrafficRatio", 0.5); // 示例占位
        } catch (Exception e) {
            initializeDefaultNetworkData(result);
        }
        return result;
    }

    private Map<String, Object> analyzeAccessData(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(ANALYSIS_DAYS);
            // ✅ 已修正：Repository 现在接收 Long
            List<AccessLog> logs = accessLogRepository.findByUserIdAndEntryTimeAfter(userId, startTime);

            result.put("libraryCount", logs.stream().filter(l -> "图书馆".equals(l.getLocationType())).count());
            result.put("classroomCount", logs.stream().filter(l -> "教学楼".equals(l.getLocationType())).count());
            result.put("lateReturnCount", logs.stream().filter(l -> {
                if (l.getEntryTime() == null) return false;
                Calendar cal = Calendar.getInstance();
                cal.setTime(l.getEntryTime());
                return cal.get(Calendar.HOUR_OF_DAY) >= 23;
            }).count());
            result.put("activeDays", 5L); // 示例占位
        } catch (Exception e) {
            initializeDefaultAccessData(result);
        }
        return result;
    }

    private Map<String, Object> analyzeBorrowData(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDate startDate = LocalDate.now().minusDays(ANALYSIS_DAYS);
            // ✅ 已修正：Repository 现在接收 Long
            List<BorrowLog> logs = borrowLogRepository.findByUserIdAndBorrowDateAfter(userId, startDate);

            result.put("borrowCount", (long) logs.size());
            result.put("borrowActivityScore", Math.min(logs.size() * 10.0, 100.0));
            result.put("unreturnedCount", 0L);
        } catch (Exception e) {
            initializeDefaultBorrowData(result);
        }
        return result;
    }

    // ==================== 评分与逻辑方法 ====================

    private double calculateRiskScore(Map<String, Object> n, Map<String, Object> a, Map<String, Object> b) {
        // 简化评分算法演示
        double score = 0.0;
        if (getDoubleValue(n, "avgOnlineHours", 0) > 8) score += 30;
        if (getLongValue(a, "lateReturnCount", 0) > 3) score += 20;
        return Math.min(score, 100.0);
    }

    private AnalysisData createAnalysisData(Long userId, double riskScore, Map<String, Object> n, Map<String, Object> a, Map<String, Object> b) {
        AnalysisData data = new AnalysisData();
        data.setUserId(userId);
        data.setAnalysisDate(LocalDate.now());
        data.setRiskScore(riskScore);
        data.setHealthScore(100 - riskScore);
        data.setAvgOnlineHours(getDoubleValue(n, "avgOnlineHours", 0));
        data.setClassroomAccessCount((int) getLongValue(a, "classroomCount", 0));
        data.setLibraryAccessCount((int) getLongValue(a, "libraryCount", 0));
        data.setLateReturnCount((int) getLongValue(a, "lateReturnCount", 0));
        data.setActiveDays((int) getLongValue(a, "activeDays", 0));
        data.setBorrowActivityScore(getDoubleValue(b, "borrowActivityScore", 0));
        data.setUnreturnedCount((int) getLongValue(b, "unreturnedCount", 0));
        return data;
    }

    private void generateOrUpdateWarning(Long userId, double riskScore) {
        try {
            String warningLevel = determineWarningType(riskScore);

            if (riskScore >= HIGH_RISK_THRESHOLD) {
                // ✅ 调用已修正为 Long 参数的 Repository 方法
                List<RiskWarning> existingWarnings = riskWarningRepository.findByUserIdAndStatus(userId, 0);

                if (existingWarnings.isEmpty()) {
                    RiskWarning warning = new RiskWarning();
                    // 假设实体类底层 ID 为 Integer，这里做安全转换
                    warning.setUserId(Math.toIntExact(userId));
                    warning.setRiskScore((int) riskScore);
                    warning.setWarningLevel(warningLevel);
                    warning.setWarningType(warningLevel);
                    warning.setCreateTime(new Date());
                    warning.setStatus(0);
                    warning.setRiskDescription("风险评分偏高");
                    riskWarningRepository.save(warning);
                } else {
                    RiskWarning warning = existingWarnings.get(0);
                    warning.setRiskScore((int) riskScore);
                    warning.setWarningType(warningLevel);
                    riskWarningRepository.save(warning);
                }
            }
        } catch (Exception e) {
            logger.error("生成预警异常", e);
        }
    }

    private String determineWarningType(double riskScore) {
        if (riskScore >= 80) return "极高风险";
        if (riskScore >= 70) return "高风险";
        if (riskScore >= 50) return "中等风险";
        return "低风险";
    }

    // ==================== 查询与统计方法 ====================

    /**
     * 获取用户预警列表
     * ✅ 已修正：只保留这一个定义，参数为 Long
     */
    public List<RiskWarning> getUserWarnings(Long userId) {
        logger.debug("获取用户 {} 的预警列表", userId);
        try {
            return riskWarningRepository.findByUserIdOrderByCreateTimeDesc(userId);
        } catch (Exception e) {
            logger.error("获取用户 {} 预警列表异常", userId, e);
            return new ArrayList<>();
        }
    }

    public AnalysisData getUserAnalysisDetail(Long userId) {
        return analysisDataRepository.findByUserId(userId).stream()
                .max(Comparator.comparing(AnalysisData::getAnalysisDate))
                .orElse(null);
    }

    // ==================== 辅助与初始化方法 ====================

    private void initializeDefaultNetworkData(Map<String, Object> r) { r.put("avgOnlineHours", 0.0); }
    private void initializeDefaultAccessData(Map<String, Object> r) { r.put("libraryCount", 0L); r.put("classroomCount", 0L); r.put("lateReturnCount", 0L); }
    private void initializeDefaultBorrowData(Map<String, Object> r) { r.put("borrowActivityScore", 0.0); r.put("unreturnedCount", 0L); }

    private double getDoubleValue(Map<String, Object> map, String key, double def) {
        Object v = map.get(key);
        return (v instanceof Number) ? ((Number) v).doubleValue() : def;
    }

    private long getLongValue(Map<String, Object> map, String key, long def) {
        Object v = map.get(key);
        return (v instanceof Number) ? ((Number) v).longValue() : def;
    }
}