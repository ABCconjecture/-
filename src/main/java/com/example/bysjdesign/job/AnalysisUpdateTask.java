package com.example.bysjdesign.job;

import com.example.bysjdesign.service.MultiDimensionalAnalysisService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 多维度分析更新任务
 * 执行时间：每天 01:00
 * 执行周期：每24小时执行一次
 *
 * 功能：
 * 1. 计算用户多维度分析数据
 * 2. 自动生成风险预警
 * 3. 更新健康度评分
 */
@Component
public class AnalysisUpdateTask implements Job {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisUpdateTask.class);

    @Autowired
    private MultiDimensionalAnalysisService analysisService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("========== 开始执行：多维度分析更新任务 ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 执行多维度分析
            logger.info("→ 正在计算用户多维度分析数据...");
            analysisService.analyzeAllUsers();

            logger.info("→ 正在自动生成风险预警...");
            analysisService.generateWarnings();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("========== 任务执行成功！耗时: {}ms ==========", duration);

        } catch (Exception e) {
            logger.error("========== 任务执行失败！==========", e);
            throw new JobExecutionException("多维度分析任务执行失败", e);
        }
    }
}