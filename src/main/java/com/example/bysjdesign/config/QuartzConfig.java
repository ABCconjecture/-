package com.example.bysjdesign.config;

import com.example.bysjdesign.job.AnalysisUpdateTask;
import com.example.bysjdesign.job.ProfileUpdateTask;
import com.example.bysjdesign.job.WarningCheckTask;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz定时任务配置
 */
@Configuration
public class QuartzConfig {

    // ==================== 多维度分析任务 ====================

    /**
     * 创建多维度分析任务Job
     * 执行时间：每天 01:00
     */
    @Bean
    public JobDetail analysisUpdateJobDetail() {
        return JobBuilder.newJob(AnalysisUpdateTask.class)
                .withIdentity("analysisUpdateJob", "group1")
                .withDescription("多维度分析更新任务")
                .storeDurably()
                .build();
    }

    /**
     * 创建多维度分析任务Trigger
     * 触发规则：每天 01:00 执行
     */
    @Bean
    public Trigger analysisUpdateTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(analysisUpdateJobDetail())
                .withIdentity("analysisUpdateTrigger", "group1")
                .withDescription("每天 01:00 执行多维度分析")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?"))
                .build();
    }

    // ==================== 用户画像任务 ====================

    /**
     * 创建用户画像更新任务Job
     * 执行时间：每周日 02:00
     */
    @Bean
    public JobDetail profileUpdateJobDetail() {
        return JobBuilder.newJob(ProfileUpdateTask.class)
                .withIdentity("profileUpdateJob", "group1")
                .withDescription("用户画像更新任务")
                .storeDurably()
                .build();
    }

    /**
     * 创建用户画像更新任务Trigger
     * 触发规则：每周日 02:00 执行
     */
    @Bean
    public Trigger profileUpdateTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(profileUpdateJobDetail())
                .withIdentity("profileUpdateTrigger", "group1")
                .withDescription("每周日 02:00 执行用户画像更新")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 ? * SUN"))
                .build();
    }

    // ==================== 预警检查任务 ====================

    /**
     * 创建预警检查任务Job
     * 执行时间：每天 03:00
     */
    @Bean
    public JobDetail warningCheckJobDetail() {
        return JobBuilder.newJob(WarningCheckTask.class)
                .withIdentity("warningCheckJob", "group1")
                .withDescription("预警检查任务")
                .storeDurably()
                .build();
    }

    /**
     * 创建预警检查任务Trigger
     * 触发规则：每天 03:00 执行
     */
    @Bean
    public Trigger warningCheckTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(warningCheckJobDetail())
                .withIdentity("warningCheckTrigger", "group1")
                .withDescription("每天 03:00 执行预警检查")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?"))
                .build();
    }

    // ==================== Scheduler配置 ====================

    /**
     * 创建Scheduler调度器
     */
    @Bean
    public Scheduler scheduler(
            JobDetail analysisUpdateJobDetail,
            Trigger analysisUpdateTrigger,
            JobDetail profileUpdateJobDetail,
            Trigger profileUpdateTrigger,
            JobDetail warningCheckJobDetail,
            Trigger warningCheckTrigger) throws SchedulerException {

        SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        // 注册所有的Job和Trigger
        scheduler.scheduleJob(analysisUpdateJobDetail, analysisUpdateTrigger);
        scheduler.scheduleJob(profileUpdateJobDetail, profileUpdateTrigger);
        scheduler.scheduleJob(warningCheckJobDetail, warningCheckTrigger);

        if (!scheduler.isStarted()) {
            scheduler.start();
        }

        return scheduler;
    }
}