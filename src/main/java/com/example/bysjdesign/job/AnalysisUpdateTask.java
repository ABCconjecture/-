package com.example.bysjdesign.job;

import com.example.bysjdesign.service.MultiDimensionalAnalysisService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnalysisUpdateTask implements Job {
    @Autowired
    private MultiDimensionalAnalysisService analysisService;

    @Override
    public void execute(JobExecutionContext context) {
        // ✅ 修正：移除不存在的方法调用 generateWarnings()
        // analyzeAllUsers 内部已集成预警生成逻辑
        analysisService.analyzeAllUsers();
    }
}