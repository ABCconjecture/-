package com.example.bysjdesign.job;

import com.example.bysjdesign.service.AcademicWarningService;
import com.example.bysjdesign.service.WarningService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 预警检查任务
 * 执行时间：每天 03:00
 * 执行周期：每24小时执行一次
 *
 * 功能：
 * 1. 综合预警检查
 * 2. 学业预警生成
 * 3. 数据一致性验证
 */
@Component
public class WarningCheckTask implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WarningCheckTask.class);

    @Autowired
    private WarningService warningService;

    @Autowired
    private AcademicWarningService academicWarningService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("========== 开始执行：预警检查任务 ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 第一步：综合预警检查
            logger.info("→ [第一步] 正在执行综合预警检查...");
            long checkStartTime = System.currentTimeMillis();
            warningService.checkAllWarnings();
            long checkDuration = System.currentTimeMillis() - checkStartTime;
            logger.info("→ [第一步] 综合预警检查完成！耗时: {}ms", checkDuration);

            // 第二步：学业预警生成
            logger.info("→ [第二步] 正在生成学业预警...");
            long academicStartTime = System.currentTimeMillis();
            academicWarningService.generateAcademicWarnings();
            long academicDuration = System.currentTimeMillis() - academicStartTime;
            logger.info("→ [第二步] 学业预警生成完成！耗时: {}ms", academicDuration);

            // 第三步：数据一致性验证
            logger.info("→ [第三步] 正在验证数据一致性...");
            long validateStartTime = System.currentTimeMillis();
            warningService.validateDataConsistency();
            long validateDuration = System.currentTimeMillis() - validateStartTime;
            logger.info("→ [第三步] 数据一致性验证完成！耗时: {}ms", validateDuration);

            long totalDuration = System.currentTimeMillis() - startTime;
            logger.info("========== 任务执行成功！总耗时: {}ms ==========", totalDuration);

        } catch (Exception e) {
            logger.error("========== 任务执行失败！==========", e);
            throw new JobExecutionException("预警检查任务执行失败", e);
        }
    }
}