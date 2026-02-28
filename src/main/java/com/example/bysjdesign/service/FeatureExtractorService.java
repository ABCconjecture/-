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
import java.time.temporal.Temporal;
import java.util.*;

/**
 * 特征提取服务
 * 从原始数据中提取关键特征用于K-means聚类
 */
@Service
public class FeatureExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureExtractorService.class);

    @Autowired
    private NetworkLogRepository networkLogRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private BorrowLogRepository borrowLogRepository;

    @Autowired
    private com.example.bysjdesign.repository.AnalysisDataRepository analysisDataRepository;

    @Autowired
    private CampusUserRepository userRepository;

    /**
     * 为所有用户提取特征
     */
    public void extractAllUserFeatures() {
        logger.info("开始提取所有用户特征...");
        try {
            List<CampusUser> users = userRepository.findAll();
            int total = users.size();
            int processed = 0;

            for (CampusUser user : users) {
                try {
                    extractUserFeatures(user.getUserId());
                    processed++;

                    if (processed % 100 == 0) {
                        logger.info("特征提取进度: {}/{}", processed, total);
                    }
                } catch (Exception e) {
                    logger.error("提取用户 {} 特征失败", user.getUserId(), e);
                }
            }

            logger.info("特征提取完成！共处理 {} 个用户", total);
        } catch (Exception e) {
            logger.error("特征提取异常", e);
            throw new RuntimeException("特征提取失败", e);
        }
    }

    /**
     * 为指定用户提取特征
     */
    public Map<String, Double> extractUserFeatures(Integer userId) {
        logger.debug("提取用户 {} 的特征", userId);

        Map<String, Double> features = new HashMap<>();

        try {
            // 1. 网络行为特征
            extractNetworkFeatures(userId, features);

            // 2. 门禁行为特征
            extractAccessFeatures(userId, features);

            // 3. 借阅行为特征
            extractBorrowFeatures(userId, features);

            // 4. 综合特征
            extractCompositeFeatures(userId, features);

            return features;
        } catch (Exception e) {
            logger.error("提取用户特征失败", e);
            return features;
        }
    }

    /**
     * 提取网络行为特征
     */
    private void extractNetworkFeatures(Integer userId, Map<String, Double> features) {
        // 获取最近30天的网络日志
        LocalDate startDate = LocalDate.now().minusDays(30);
        List<NetworkLog> logs = networkLogRepository.findByUserIdAndSessionStartAfter(
                userId,
                startDate.atStartOfDay()
        );

        if (logs.isEmpty()) {
            features.put("avgDailyOnlineHours", 0.0);
            features.put("studyTrafficRatio", 0.0);
            features.put("peakHour", 12.0);
            return;
        }

        // 计算日均上网时长
        double totalHours = logs.stream()
                .mapToDouble(log -> (log.getSessionEnd().getTime() - log.getSessionStart().getTime()) / 3600000.0)
                .sum();
        double avgDailyHours = totalHours / 30;
        features.put("avgDailyOnlineHours", avgDailyHours);

        // 计算学习流量占比
        long studyTraffic = logs.stream()
                .filter(log -> "学习".equals(log.getCategory()))
                .mapToLong(NetworkLog::getDataVolume)
                .sum();
        long totalTraffic = logs.stream()
                .mapToLong(NetworkLog::getDataVolume)
                .sum();
        double studyRatio = totalTraffic > 0 ? (double) studyTraffic / totalTraffic : 0.0;
        features.put("studyTrafficRatio", studyRatio);

        // 计算高峰时段
        OptionalDouble peakHour = logs.stream()
                .mapToDouble(log -> log.getSessionStart().getHours())
                .average();
        features.put("peakHour", peakHour.orElse(12.0));
    }

    /**
     * 提取门禁行为特征
     */
    private void extractAccessFeatures(Integer userId, Map<String, Double> features) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        List<AccessLog> logs = accessLogRepository.findByUserIdAndEntryTimeAfter(
                userId,
                startDate.atStartOfDay()
        );

        // 计算教学楼进出次数
        long classroomCount = logs.stream()
                .filter(log -> "教学楼".equals(log.getLocationType()))
                .count();
        features.put("classroomAccessCount", (double) classroomCount);

        // 计算晚归次数（23点后进入）
        long lateReturnCount = logs.stream()
                .filter(log -> log.getEntryTime().getHours() >= 23)
                .count();
        features.put("lateReturnCount", (double) lateReturnCount);

        // 计算外出频率
        double accessFrequency = logs.size() / 30.0;
        features.put("accessFrequency", accessFrequency);
    }

    /**
     * 提取借阅行为特征
     */
    private void extractBorrowFeatures(Integer userId, Map<String, Double> features) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        List<BorrowLog> logs = borrowLogRepository.findByUserIdAndBorrowDateAfter(
                userId,
                startDate
        );

        // 计算借阅活跃度
        double borrowActivity = logs.size() / 1.0; // 简化计算
        features.put("borrowActivityScore", borrowActivity);

        // 计算平均借阅周期
        double avgBorrowDays = logs.stream()
                .mapToDouble(log -> {
                    if (log.getReturnDate() != null) {
                        return java.time.temporal.ChronoUnit.DAYS.between(
                                (Temporal) log.getBorrowDate(),
                                (Temporal) log.getReturnDate()
                        );
                    }
                    return 0;
                })
                .average()
                .orElse(0.0);

        features.put("avgBorrowDays", avgBorrowDays);
    }

    /**
     * 提取综合特征
     */
    private void extractCompositeFeatures(Integer userId, Map<String, Double> features) {
        // 获取最新的分析数据
        Pageable pageable = PageRequest.of(0, 1);
        AnalysisData analysisData = analysisDataRepository.findByUserId(userId, pageable)
                .stream()
                .max(Comparator.comparing(AnalysisData::getAnalysisDate))
                .orElse(null);

        if (analysisData != null) {
            // 添加健康度
            features.put("overallHealthScore", analysisData.getOverallHealthScore());

            // 添加异常流量标志
            features.put("abnormalTrafficFlag", analysisData.getAbnormalTrafficFlag() ? 1.0 : 0.0);

            // 添加缺勤标志
            features.put("absenteeFlag", analysisData.getAbsenteeFlag() ? 1.0 : 0.0);
        } else {
            features.put("overallHealthScore", 50.0);
            features.put("abnormalTrafficFlag", 0.0);
            features.put("absenteeFlag", 0.0);
        }
    }

    /**
     * 标准化特征（用于K-means）
     */
    public double[][] normalizeFeatures(List<Map<String, Double>> featuresList) {
        int numFeatures = featuresList.isEmpty() ? 0 : featuresList.get(0).size();
        int numUsers = featuresList.size();

        if (numUsers == 0 || numFeatures == 0) {
            return new double[0][0];
        }

        double[][] matrix = new double[numUsers][numFeatures];
        String[] featureNames = featuresList.get(0).keySet().toArray(new String[0]);

        // 填充矩阵
        for (int i = 0; i < numUsers; i++) {
            Map<String, Double> features = featuresList.get(i);
            for (int j = 0; j < numFeatures; j++) {
                matrix[i][j] = features.getOrDefault(featureNames[j], 0.0);
            }
        }

        // 标准化每一列
        for (int j = 0; j < numFeatures; j++) {
            double mean = 0;
            double sum = 0;

            // 计算均值
            for (int i = 0; i < numUsers; i++) {
                sum += matrix[i][j];
            }
            mean = sum / numUsers;

            // 计算标准差
            double variance = 0;
            for (int i = 0; i < numUsers; i++) {
                variance += Math.pow(matrix[i][j] - mean, 2);
            }
            double stdDev = Math.sqrt(variance / numUsers);

            // 标准化
            if (stdDev > 0) {
                for (int i = 0; i < numUsers; i++) {
                    matrix[i][j] = (matrix[i][j] - mean) / stdDev;
                }
            }
        }

        return matrix;
    }
}