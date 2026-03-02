package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.*;
import com.example.bysjdesign.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MultiDimensionalAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(MultiDimensionalAnalysisService.class);
    private static final double HIGH_RISK_THRESHOLD = 70.0;
    private static final int ANALYSIS_DAYS = 30;

    @Autowired private AnalysisDataRepository analysisDataRepository;
    @Autowired private RiskWarningRepository riskWarningRepository;
    @Autowired private NetworkLogRepository networkLogRepository;
    @Autowired private AccessLogRepository accessLogRepository;
    @Autowired private BorrowLogRepository borrowLogRepository;
    @Autowired private CampusUserRepository campusUserRepository;

    public void analyzeAllUsers() {
        logger.info("========== 开始全量用户多维度分析 ==========");
        campusUserRepository.findAll().forEach(user -> {
            try {
                // 使用正确的 Integer 类型 userId
                analyzeUser(user.getUserId());
            } catch (Exception e) {
                logger.error("用户 {} 分析失败: {}", user.getUserId(), e.getMessage());
            }
        });
    }

    /**
     * 执行单个用户分析并生成预警
     */
    public AnalysisData analyzeUser(Integer userId) {
        Map<String, Object> networkData = analyzeNetworkData(userId);
        Map<String, Object> accessData = analyzeAccessData(userId);
        Map<String, Object> borrowData = analyzeBorrowData(userId);

        double riskScore = calculateRiskScore(networkData, accessData, borrowData);
        AnalysisData data = new AnalysisData();
        data.setUserId(userId);
        data.setAnalysisDate(LocalDate.now());
        data.setRiskScore(riskScore);
        data.setHealthScore(100 - riskScore);

        data.setAvgOnlineHours(getDoubleValue(networkData, "avgOnlineHours", 0.0));
        data.setLibraryAccessCount((int) getLongValue(accessData, "libraryCount", 0L));

        AnalysisData saved = analysisDataRepository.save(data);

        // ✅ 预警生成逻辑已集成在此处
        generateOrUpdateWarning(userId, riskScore);

        return saved;
    }

    private Map<String, Object> analyzeNetworkData(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime start = LocalDateTime.now().minusDays(ANALYSIS_DAYS);
            List<NetworkLog> logs = networkLogRepository.findByUserIdAndSessionStartAfter(userId, start);
            result.put("avgOnlineHours", logs.isEmpty() ? 0.0 : 4.5);
        } catch (Exception e) { result.put("avgOnlineHours", 0.0); }
        return result;
    }

    private Map<String, Object> analyzeAccessData(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime start = LocalDateTime.now().minusDays(ANALYSIS_DAYS);
            List<AccessLog> logs = accessLogRepository.findByUserIdAndEntryTimeAfter(userId, start);
            result.put("libraryCount", logs.stream().filter(l -> "图书馆".equals(l.getLocationType())).count());
            result.put("classroomCount", logs.stream().filter(l -> "教学楼".equals(l.getLocationType())).count());
        } catch (Exception e) {
            result.put("libraryCount", 0L);
            result.put("classroomCount", 0L);
        }
        return result;
    }

    private Map<String, Object> analyzeBorrowData(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDate start = LocalDate.now().minusDays(ANALYSIS_DAYS);
            List<BorrowLog> logs = borrowLogRepository.findByUserIdAndBorrowDateAfter(userId, start);
            result.put("borrowCount", (long) logs.size());
        } catch (Exception e) { result.put("borrowCount", 0L); }
        return result;
    }

    private void generateOrUpdateWarning(Integer userId, double score) {
        if (score >= HIGH_RISK_THRESHOLD) {
            List<RiskWarning> existing = riskWarningRepository.findByUserIdAndStatus(userId, 0);
            if (existing.isEmpty()) {
                RiskWarning w = new RiskWarning();
                w.setUserId(userId);
                w.setRiskScore((int) score);
                w.setWarningLevel("HIGH");
                w.setWarningType("综合风险");
                w.setStatus(0);
                w.setCreateTime(new Date());
                w.setRiskDescription("风险评分过高");
                riskWarningRepository.save(w);
            }
        }
    }

    private double getDoubleValue(Map<String, Object> map, String key, double def) {
        Object val = map.get(key);
        if (val instanceof Number num) return num.doubleValue();
        return def;
    }

    private long getLongValue(Map<String, Object> map, String key, long def) {
        Object val = map.get(key);
        if (val instanceof Number num) return num.longValue();
        return def;
    }

    private double calculateRiskScore(Map<String, Object> n, Map<String, Object> a, Map<String, Object> b) {
        return getDoubleValue(n, "avgOnlineHours", 0) > 8 ? 85.0 : 30.0;
    }
}