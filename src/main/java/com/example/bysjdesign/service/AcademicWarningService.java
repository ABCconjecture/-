package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.AnalysisData;
import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.campus.entity.RiskWarning;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.repository.RiskWarningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学业预警服务
 *
 * 职责：
 * 1. 生成学业预警
 * 2. 分析学生学业状态
 * 3. 管理学业风险
 */
@Service
public class AcademicWarningService {

    private static final Logger logger = LoggerFactory.getLogger(AcademicWarningService.class);

    private static final double HIGH_RISK_THRESHOLD = 70.0;
    private static final double MEDIUM_RISK_THRESHOLD = 50.0;

    @Autowired
    private CampusUserRepository campusUserRepository;

    @Autowired
    private AnalysisDataRepository analysisDataRepository;

    @Autowired
    private RiskWarningRepository riskWarningRepository;

    /**
     * 生成学业预警
     * 根据分析数据和用户状态生成相应的学业预警
     */
    @Transactional
    public void generateAcademicWarnings() {
        logger.info("开始生成学业预警...");
        try {
            List<CampusUser> allUsers = campusUserRepository.findAll();
            int warningCount = 0;

            for (CampusUser user : allUsers) {
                // 获取最近的分析数据
                LocalDate analysisDate = LocalDate.now();
                List<AnalysisData> recentAnalysis = analysisDataRepository.findByUserId(
                        user.getUserId(),
                        org.springframework.data.domain.PageRequest.of(0, 30)
                );

                if (!recentAnalysis.isEmpty()) {
                    AnalysisData latestAnalysis = recentAnalysis.get(0);
                    double overallScore = 100 - latestAnalysis.getRiskScore();

                    // 判断是否需要生成预警
                    if (latestAnalysis.getRiskScore() >= HIGH_RISK_THRESHOLD) {
                        generateWarningIfNotExists(user.getUserId(), "ACADEMIC_RISK",
                                "HIGH", latestAnalysis.getRiskScore());
                        warningCount++;
                    } else if (latestAnalysis.getRiskScore() >= MEDIUM_RISK_THRESHOLD) {
                        generateWarningIfNotExists(user.getUserId(), "NETWORK_ADDICTION",
                                "MEDIUM", latestAnalysis.getRiskScore());
                        warningCount++;
                    }
                }
            }

            logger.info("学业预警生成完成，共生成 {} 条预警", warningCount);
        } catch (Exception e) {
            logger.error("生成学业预警失败", e);
            throw new RuntimeException("学业预警生成失败", e);
        }
    }

    /**
     * 如果不存在相同的预警，则生成新预警
     */
    private void generateWarningIfNotExists(Integer userId, String warningType,
                                            String warningLevel, double riskScore) {
        try {
            // 检查是否已存在未解决的相同类型预警
            List<RiskWarning> existingWarnings = riskWarningRepository.findByUserIdAndStatus(
                    userId.longValue(), "0");

            boolean hasExistingWarning = existingWarnings.stream()
                    .anyMatch(w -> w.getWarningType().equals(warningType) &&
                                   w.getStatus() == 0);  // 0: 未处理

            if (!hasExistingWarning) {
                RiskWarning warning = new RiskWarning();
                warning.setUserId(userId);
                warning.setWarningType(warningType);
                warning.setWarningLevel(warningLevel);
                warning.setRiskScore((int) riskScore);
                warning.setStatus(0);  // 0: 未处理
                warning.setCreateTime(new Date());
                warning.setRiskDescription("系统自动生成的学业预警");

                riskWarningRepository.save(warning);
                logger.debug("为用户 {} 生成新预警，类型: {}", userId, warningType);
            }
        } catch (Exception e) {
            logger.error("生成预警失败，userId: {}", userId, e);
        }
    }

    /**
     * 获取用户的学业风险评分
     */
    public double getAcademicRiskScore(Integer userId) {
        try {
            List<AnalysisData> recentAnalysis = analysisDataRepository.findByUserId(
                    userId,
                    org.springframework.data.domain.PageRequest.of(0, 1)
            );

            if (!recentAnalysis.isEmpty()) {
                return recentAnalysis.get(0).getRiskScore();
            }
        } catch (Exception e) {
            logger.error("获取用户学业风险评分失败，userId: {}", userId, e);
        }
        return 0.0;
    }

    /**
     * 统计预警数量
     */
    public Map<String, Long> getWarningStatistics() {
        try {
            List<RiskWarning> allWarnings = riskWarningRepository.findAll();

            return allWarnings.stream()
                    .collect(Collectors.groupingBy(
                            RiskWarning::getWarningType,
                            Collectors.counting()
                    ));
        } catch (Exception e) {
            logger.error("统计预警数量失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 解除学生预警
     */
    @Transactional
    public void clearWarning(Integer warningId, String remarks) {
        try {
            Optional<RiskWarning> warningOpt = riskWarningRepository.findById(warningId);
            if (warningOpt.isPresent()) {
                RiskWarning warning = warningOpt.get();
                warning.setStatus(2);  // 2: 已解决
                warning.setHandleTime(new Date());
                warning.setHandlerRemark(remarks);
                riskWarningRepository.save(warning);
                logger.info("预警已解除，warningId: {}", warningId);
            }
        } catch (Exception e) {
            logger.error("解除预警失败，warningId: {}", warningId, e);
        }
    }
}
