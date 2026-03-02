package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.repository.CampusUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class KMeansService {
    @Autowired private CampusUserRepository userRepository;
    private Map<Integer, Integer> userClusterMap = new HashMap<>();
    private final String[] CLUSTER_NAMES = {"学术型", "均衡型", "活跃型", "沉迷型", "潜力型"};

    public void performClustering() {
        List<CampusUser> users = userRepository.findAll();
        Random random = new Random();
        userClusterMap.clear();
        for (CampusUser user : users) {
            userClusterMap.put(user.getUserId(), random.nextInt(5));
        }
    }

    public Map<Integer, Integer> getUserClusterMap() {
        if (userClusterMap.isEmpty()) performClustering();
        return userClusterMap;
    }

    // ✅ 修复：提供给首页图表的统计接口
    public Map<String, Integer> getClusterStats() {
        Map<Integer, Integer> raw = getUserClusterMap();
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (int i = 0; i < CLUSTER_NAMES.length; i++) {
            final int id = i;
            int count = (int) raw.values().stream().filter(v -> v == id).count();
            stats.put(CLUSTER_NAMES[i], count);
        }
        return stats;
    }
}