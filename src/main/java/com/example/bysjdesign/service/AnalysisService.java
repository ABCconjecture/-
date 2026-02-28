package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.AnalysisData;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalysisService {

    @Autowired
    private AnalysisDataRepository repository;

    /**
     * 查询用户最新分析数据
     * 修复：调用 Repository 中定义的 findFirstByUserIdOrderByAnalysisDateDesc
     */
    public AnalysisData getLatestAnalysis(Long userId) {
        // 使用修正后的方法名
        return repository.findFirstByUserIdOrderByAnalysisDateDesc(userId).orElse(null);
    }

    /**
     * 查询用户一周内的分析数据
     */
    public List<AnalysisData> getWeeklyAnalysis(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        // 这里的 userId 现在是 Long，Repository 也支持 Long 了
        return repository.findByUserIdAndAnalysisDateBetween(userId, startDate, endDate);
    }

    public List<AnalysisData> getHighRiskUsers() {
        return repository.findByRiskScoreGreaterThan(70.0);
    }

    public Double getAverageHealthScore(LocalDate date) {
        return repository.getAverageHealthScoreByDate(date);
    }

    public List<Object[]> getRiskDistribution() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return repository.getRiskDistributionByDateRange(startDate, endDate);
    }
}