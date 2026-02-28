// 升级版本 - 集成多维度预警
package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.AnalysisData;
import com.example.bysjdesign.campus.entity.WarningLog;
import com.example.bysjdesign.campus.entity.RiskWarning;
import com.example.bysjdesign.repository.AccessLogRepository;
import com.example.bysjdesign.repository.BorrowLogRepository;
import com.example.bysjdesign.repository.NetworkLogRepository;
import com.example.bysjdesign.repository.WarningLogRepository;
import com.example.bysjdesign.repository.RiskWarningRepository;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class WarningService {

    @Autowired
    private NetworkLogRepository networkLogRepository;
    @Autowired
    private AccessLogRepository accessLogRepository;
    @Autowired
    private BorrowLogRepository borrowLogRepository;
    @Autowired
    private WarningLogRepository warningLogRepository;
    @Autowired
    private RiskWarningRepository riskWarningRepository;
    @Autowired
    private AnalysisDataRepository analysisDataRepository;

    /**
     * 综合预警检查
     * 基于多个数据源进行综合分析
     */
    public void checkAll() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date weekAgo = cal.getTime();

        // 检查网络行为异常
        checkNetworkAnomalies(weekAgo, now);

        // 检查考勤异常
        checkAttendanceAnomalies(weekAgo, now);

        // 检查综合健康度
        checkOverallHealth(weekAgo, now);
    }

    /**
     * 网络行为异常检查
     */
    private void checkNetworkAnomalies(Date startDate, Date endDate) {
        List<Object[]> netStats = networkLogRepository.summaryByUser(startDate, endDate);
        for (Object[] row : netStats) {
            Integer userId = ((Number) row[0]).intValue();
            Long totalDuration = ((Number) row[1]).longValue();
            Long leisureTraffic = ((Number) row[2]).longValue();
            Long totalTraffic = ((Number) row[3]).longValue();

            double leisureRatio = totalTraffic > 0 ? (double) leisureTraffic / totalTraffic : 0;
            double avgHours = totalDuration / 3600.0 / 7.0;

            if (avgHours > 8 && leisureRatio > 0.5) {
                createWarning(userId, "网络沉迷",
                        String.format("最近一周日均上网%.1f小时，休闲类占比%.1f%%", avgHours, leisureRatio * 100));
            }
        }
    }

    /**
     * 考勤异常检查
     */
    private void checkAttendanceAnomalies(Date startDate, Date endDate) {
        List<Object[]> accStats = accessLogRepository.classroomSummary(startDate, endDate);
        for (Object[] row : accStats) {
            Integer userId = ((Number) row[0]).intValue();
            Long count = (Long) row[1];

            if (count < 3) {
                createWarning(userId, "缺勤",
                        "最近一周教学楼门禁次数仅" + count + "次");
            }
        }

        // 晚归检查
        List<Object[]> lateStats = accessLogRepository.lateReturnSummary(startDate, endDate);
        for (Object[] row : lateStats) {
            Integer userId = ((Number) row[0]).intValue();
            Long lateCount = (Long) row[1];

            if (lateCount >= 3) {
                createWarning(userId, "晚归",
                        "最近一周晚归次数" + lateCount + "次");
            }
        }
    }

    /**
     * 综合健康度检查
     */
    private void checkOverallHealth(Date startDate, Date endDate) {
        List<AnalysisData> allData = analysisDataRepository.findAll();

        for (AnalysisData data : allData) {
            if (data.getOverallHealthScore() < 40) {
                createWarning(Math.toIntExact(data.getUserId()), "综合预警",
                        String.format("综合健康度评分%.0f，存在多项异常指标请关注",
                                data.getOverallHealthScore()));
            }
        }
    }

    /**
     * 创建预警记录
     */
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
    }

    public void validateDataConsistency() {
    }
}