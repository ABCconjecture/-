package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.*;
import com.example.bysjdesign.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class WarningService {

    @Autowired private NetworkLogRepository networkLogRepository;
    @Autowired private AccessLogRepository accessLogRepository;
    @Autowired private WarningLogRepository warningLogRepository;
    @Autowired private RiskWarningRepository riskWarningRepository;
    @Autowired private AnalysisDataRepository analysisDataRepository;

    public void checkAll() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date weekAgo = cal.getTime();

        checkNetworkAnomalies(weekAgo, now);
        checkAttendanceAnomalies(weekAgo, now);
        checkOverallHealth(weekAgo, now);
    }

    // 检查网络异常
    private void checkNetworkAnomalies(Date startDate, Date endDate) {
        List<Object[]> netStats = networkLogRepository.summaryByUser(startDate, endDate);
        for (Object[] row : netStats) {
            Integer userId = ((Number) row[0]).intValue();
            Long totalDuration = ((Number) row[1]).longValue();
            Long leisureTraffic = ((Number) row[2]).longValue();
            Long totalTraffic = ((Number) row[3]).longValue();
            double avgHours = totalDuration / 3600.0 / 7.0;
            double leisureRatio = totalTraffic > 0 ? (double) leisureTraffic / totalTraffic : 0;

            if (avgHours > 8 && leisureRatio > 0.5) {
                createWarning(userId, "网络沉迷", String.format("最近一周日均上网 %.1f 小时", avgHours));
            }
        }
    }

    // 检查考勤异常
    private void checkAttendanceAnomalies(Date startDate, Date endDate) {
        List<Object[]> accStats = accessLogRepository.classroomSummary(startDate, endDate);
        for (Object[] row : accStats) {
            Integer userId = ((Number) row[0]).intValue();
            if ((Long) row[1] < 3) createWarning(userId, "缺勤", "最近一周教学楼出勤偏低");
        }
    }

    // 检查综合健康度
    private void checkOverallHealth(Date startDate, Date endDate) {
        List<AnalysisData> allData = analysisDataRepository.findAll();
        for (AnalysisData data : allData) {
            if (data.getHealthScore() != null && data.getHealthScore() < 40) {
                createWarning(data.getUserId(), "综合预警", "综合健康度评分过低");
            }
        }
    }

    private void createWarning(Integer userId, String type, String content) {
        WarningLog warn = new WarningLog();
        warn.setUserId(userId);
        warn.setType(type);
        warn.setContent(content);
        warn.setCreateTime(new Date());
        warn.setStatus(0);
        warningLogRepository.save(warn);
    }

    public void checkAllWarnings() {
        checkAll(); // 调用主逻辑
    }

    public void validateDataConsistency() {}
}