package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.AnalysisData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisDataRepository extends JpaRepository<AnalysisData, Long> {

    // ==================== 基础查询 ====================

    // 将 Integer 改为 Long，匹配 Service 层的需求
    List<AnalysisData> findByUserId(Long userId);

    /**
     * 根据用户ID查询最新的分析数据
     * 注意：方法名必须严格遵守 JPA 规范
     */
    Optional<AnalysisData> findFirstByUserIdOrderByAnalysisDateDesc(Long userId);

    List<AnalysisData> findByAnalysisDate(LocalDate date);

    List<AnalysisData> findByAnalysisDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // 将 Integer 改为 Long
    List<AnalysisData> findByUserIdAndAnalysisDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // ==================== 风险评分查询 ====================

    List<AnalysisData> findByRiskScoreGreaterThan(Double riskScore);

    List<AnalysisData> findByHealthScoreGreaterThan(Double healthScore);

    // ==================== 统计查询 ====================

    long countByRiskScoreGreaterThan(Double riskScore);

    // ==================== 聚合查询 ====================

    @Query("SELECT AVG(a.healthScore) FROM AnalysisData a WHERE a.analysisDate = :date")
    Double getAverageHealthScoreByDate(@Param("date") LocalDate date);

    // ==================== 删除操作 ====================

    long deleteByAnalysisDateBefore(LocalDate date);

    // 将 Integer 改为 Long
    long deleteByUserId(Long userId);

    // ==================== 复杂查询 ====================

    @Query("SELECT " +
            "CASE " +
            "WHEN a.riskScore >= 80 THEN '极高风险' " +
            "WHEN a.riskScore >= 70 THEN '高风险' " +
            "WHEN a.riskScore >= 50 THEN '中等风险' " +
            "ELSE '低风险' END as riskLevel, " +
            "COUNT(*) as count " +
            "FROM AnalysisData a " +
            "WHERE a.analysisDate BETWEEN :startDate AND :endDate " +
            "GROUP BY riskLevel " +
            "ORDER BY COUNT(*) DESC")
    List<Object[]> getRiskDistributionByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 修正参数类型为 Long
    List<AnalysisData> findByUserId(Long userId, Pageable pageable);

    List<AnalysisData> findLatestAnalysisForUsers(List<Long> userIds);
}