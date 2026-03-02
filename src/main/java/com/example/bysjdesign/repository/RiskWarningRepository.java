package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.RiskWarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RiskWarningRepository extends JpaRepository<RiskWarning, Integer> {

    List<RiskWarning> findByStatusOrderByCreateTimeDesc(Integer status);

    /**
     * ✅ 修复：参数类型改为 Integer，与 RiskWarning 实体类中的 userId 字段一致
     */
    List<RiskWarning> findByUserIdOrderByCreateTimeDesc(Integer userId);

    List<RiskWarning> findByUserIdAndStatus(Integer userId, Integer status);
}