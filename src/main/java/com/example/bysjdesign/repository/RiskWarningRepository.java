package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.RiskWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RiskWarningRepository extends JpaRepository<RiskWarning, Integer> {

    /** ✅ 统一参数为 Long */
    List<RiskWarning> findByUserIdAndWarningLevelOrderByCreateTimeDesc(Long userId, String warningLevel);

    /** ✅ 统一参数为 Long */
    List<RiskWarning> findByUserIdOrderByCreateTimeDesc(Long userId);

    /** ✅ 修正状态查询：通常 status 是 Integer，参数统一为 Long */
    List<RiskWarning> findByUserIdAndStatus(Long userId, Integer status);

    List<RiskWarning> findByStatusOrderByCreateTimeDesc(Integer status);

    Page<RiskWarning> findByStatusAndWarningLevel(Integer status, String warningLevel, Pageable pageable);

    @Query("SELECT w.warningType, COUNT(w) FROM RiskWarning w GROUP BY w.warningType")
    List<Object[]> countByType();

    @Query("SELECT DISTINCT w.userId FROM RiskWarning w WHERE w.warningLevel = 'HIGH' AND w.status = 0 AND w.createTime >= :startDate")
    List<Long> findHighRiskUsers(@Param("startDate") Date startDate);
}