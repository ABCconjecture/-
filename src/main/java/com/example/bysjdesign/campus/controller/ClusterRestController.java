package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.repository.UserProfileRepository;
import com.example.bysjdesign.service.UserProfileService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors; // ✅ 修复：添加缺失的导入

@RestController
@RequestMapping("/api/campus/cluster")
public class ClusterRestController {

    private final UserProfileRepository userProfileRepository;
    private final CampusUserRepository userRepository;
    private final UserProfileService userProfileService;

    // ✅ 修复：改用构造器注入，解决字段注入警告
    public ClusterRestController(UserProfileRepository userProfileRepository,
                                 CampusUserRepository userRepository,
                                 UserProfileService userProfileService) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/counts")
    public Map<String, Object> getClusterCounts() {
        Map<String, Integer> clusterCounts = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            final int clusterId = i;
            long count = userProfileRepository.findAll().stream()
                    .filter(p -> p.getClusterId() != null && p.getClusterId() == clusterId).count();
            clusterCounts.put("cluster_" + i, (int) count);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", clusterCounts);
        return result;
    }

    @PostMapping("/trigger")
    public Map<String, Object> triggerProfileUpdate() {
        try {
            userProfileService.generateUserProfiles();
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "画像更新任务已启动");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "执行失败: " + e.getMessage());
            return result;
        }
    }

    @GetMapping("/{clusterId}/users")
    public Map<String, Object> getClusterUsers(@PathVariable Integer clusterId) {
        List<UserProfile> clusterProfiles = userProfileRepository.findAll().stream()
                .filter(p -> p.getClusterId() != null && p.getClusterId().equals(clusterId))
                .collect(Collectors.toList());

        List<Map<String, Object>> userDetails = clusterProfiles.stream().map(profile -> {
            Map<String, Object> userInfo = new HashMap<>();
            CampusUser user = userRepository.findById(profile.getUserId()).orElse(null);
            if (user != null) {
                userInfo.put("userId", user.getUserId());
                userInfo.put("name", user.getName());
                userInfo.put("studentId", user.getStudentId());
                userInfo.put("tags", profile.getTags());
            }
            return userInfo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", userDetails);
        return result;
    }
}