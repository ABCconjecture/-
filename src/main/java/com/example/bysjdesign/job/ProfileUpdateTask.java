package com.example.bysjdesign.job;

import com.example.bysjdesign.service.FeatureExtractorService;
import com.example.bysjdesign.service.KMeansService;
import com.example.bysjdesign.service.UserProfileService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户画像更新任务
 * 执行时间：每周日 02:00
 * 执行周期：每周执行一次
 *
 * 功能：
 * 1. 特征提取（30分钟）
 * 2. K-means聚类（30分钟）
 * 3. 生成用户画像
 */
@Component
public class ProfileUpdateTask implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ProfileUpdateTask.class);

    @Autowired
    private FeatureExtractorService featureExtractorService;

    @Autowired
    private KMeansService kMeansService;

    @Autowired
    private UserProfileService userProfileService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("========== 开始执行：用户画像更新任务 ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 第一步：特征提取
            logger.info("→ [第一步] 正在执行特征提取...");
            long extractStartTime = System.currentTimeMillis();
            featureExtractorService.extractAllUserFeatures();
            long extractDuration = System.currentTimeMillis() - extractStartTime;
            logger.info("→ [第一步] 特征提取完成！耗时: {}ms", extractDuration);

            // 第二步：K-means聚类
            logger.info("→ [第二步] 正在执行K-means聚类...");
            long clusterStartTime = System.currentTimeMillis();
            kMeansService.performClustering();
            long clusterDuration = System.currentTimeMillis() - clusterStartTime;
            logger.info("→ [第二步] 聚类完成！耗时: {}ms", clusterDuration);

            // 第三步：生成用户画像
            logger.info("→ [第三步] 正在生成用户画像...");
            long profileStartTime = System.currentTimeMillis();
            userProfileService.generateUserProfiles();
            long profileDuration = System.currentTimeMillis() - profileStartTime;
            logger.info("→ [第三步] 用户画像生成完成！耗时: {}ms", profileDuration);

            long totalDuration = System.currentTimeMillis() - startTime;
            logger.info("========== 任务执行成功！总耗时: {}ms ==========", totalDuration);

        } catch (Exception e) {
            logger.error("========== 任务执行失败！==========", e);
            throw new JobExecutionException("用户画像更新任务执行失败", e);
        }
    }
}