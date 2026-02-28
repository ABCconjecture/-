package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.AnalysisData;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptimizedAnalysisService {
    @Autowired
    private AnalysisDataRepository analysisDataRepository;

    /** ✅ 修复：Integer userId -> Long userId */
    public Page<AnalysisData> getUserAnalysisHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "analysisDate");
        return (Page<AnalysisData>) analysisDataRepository.findByUserId(userId, pageable);
    }

    /** ✅ 修复：List<Integer> -> List<Long> */
    public List<AnalysisData> getLatestAnalysisForUsers(List<Long> userIds) {
        return analysisDataRepository.findLatestAnalysisForUsers(userIds);
    }

    // ... getAnalysisDataByDateRange 等方法保持不变 ...
}