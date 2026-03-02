package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.AnalysisData;
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

    List<AnalysisData> findByUserId(Integer userId, Pageable pageable);

    // ✅ 修复：多维度分析所需的历史报告查询
    List<AnalysisData> findByUserIdAndAnalysisDateBetween(Integer userId, LocalDate start, LocalDate end);

    // ✅ 修复：首页统计所需的最新记录查询
    Optional<AnalysisData> findFirstByUserIdOrderByAnalysisDateDesc(Integer userId);

    @Query("SELECT a FROM AnalysisData a WHERE a.id IN (" +
            "  SELECT MAX(a2.id) FROM AnalysisData a2 WHERE a2.userId IN :userIds GROUP BY a2.userId" +
            ")")
    List<AnalysisData> findLatestAnalysisForUsers(@Param("userIds") List<Integer> userIds);

    // ✅ 修复：首页系统统计数据来源
    @Query("SELECT COUNT(a) FROM AnalysisData a WHERE a.riskScore > 70")
    long countHighRiskUsers();

    @Query("SELECT AVG(a.healthScore) FROM AnalysisData a")
    Double getGlobalAverageHealthScore();
}