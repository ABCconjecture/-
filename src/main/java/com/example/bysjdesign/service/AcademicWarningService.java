package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.*;
import com.example.bysjdesign.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AcademicWarningService {
    @Autowired private CampusUserRepository userRepository;
    @Autowired private AnalysisDataRepository analysisRepository;
    @Autowired private RiskWarningRepository warningRepository;

    public void generateAcademicWarnings() {
        userRepository.findAll().forEach(user -> {
            Optional<AnalysisData> latest = analysisRepository.findFirstByUserIdOrderByAnalysisDateDesc(user.getId());
            latest.ifPresent(data -> {
                if (data.getRiskScore() >= 70.0) {
                    saveWarningIfNotExists(user.getId(), "ACADEMIC_RISK", data.getRiskScore());
                }
            });
        });
    }

    private void saveWarningIfNotExists(Integer userId, String type, Double score) {
        // 修正：参数对齐 Long userId 和 Integer 0
        List<RiskWarning> existing = warningRepository.findByUserIdAndStatus(userId, 0);
        boolean exists = existing.stream().anyMatch(w -> w.getWarningType().equals(type));

        if (!exists) {
            RiskWarning w = new RiskWarning();
            w.setUserId(userId.intValue());
            w.setWarningType(type);
            w.setRiskScore(score.intValue());
            w.setStatus(0);
            w.setWarningLevel("HIGH");
            w.setCreateTime(new Date());
            w.setRiskDescription("学业风险自动检测");
            warningRepository.save(w);
        }
    }
}