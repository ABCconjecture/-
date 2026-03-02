package com.example.bysjdesign.config;

import com.example.bysjdesign.job.AnalysisUpdateTask;
import com.example.bysjdesign.job.ProfileUpdateTask;
import com.example.bysjdesign.job.WarningCheckTask;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    // 1. 定义分析任务详情
    @Bean
    public JobDetail analysisUpdateJobDetail() {
        return JobBuilder.newJob(AnalysisUpdateTask.class)
                .withIdentity("analysisUpdateJob")
                .storeDurably()
                .build();
    }

    // 2. 定义触发器 (每天 01:00)
    @Bean
    public Trigger analysisUpdateTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(analysisUpdateJobDetail())
                .withIdentity("analysisUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?"))
                .build();
    }

    // 画像更新任务 (每周日 02:00)
    @Bean
    public JobDetail profileUpdateJobDetail() {
        return JobBuilder.newJob(ProfileUpdateTask.class).withIdentity("profileUpdateJob").storeDurably().build();
    }

    @Bean
    public Trigger profileUpdateTrigger() {
        return TriggerBuilder.newTrigger().forJob(profileUpdateJobDetail())
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 ? * SUN")).build();
    }

    // 预警检查任务 (每天 03:00)
    @Bean
    public JobDetail warningCheckJobDetail() {
        return JobBuilder.newJob(WarningCheckTask.class).withIdentity("warningCheckJob").storeDurably().build();
    }

    @Bean
    public Trigger warningCheckTrigger() {
        return TriggerBuilder.newTrigger().forJob(warningCheckJobDetail())
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?")).build();
    }
}