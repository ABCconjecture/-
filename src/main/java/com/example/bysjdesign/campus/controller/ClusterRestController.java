package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚类API控制器
 * 提供用户聚类分析相关REST接口
 */
@RestController
@RequestMapping("/api/campus/cluster")
@CrossOrigin(origins = "http://localhost:3000")
public class ClusterRestController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CampusUserRepository userRepository;

    /**
     * 获取聚类计数
     * 返回各聚类中的用户数量
     */
    @GetMapping("/counts")
    public Map<String, Object> getClusterCounts() {
        Map<String, Integer> clusterCounts = new HashMap<>();

        // 统计每个聚类的用户数
        for (int i = 0; i < 5; i++) {
            final int clusterId = i;
            long count = userProfileRepository.findAll().stream()
                    .filter(p -> p.getClusterId() == clusterId)
                    .count();
            clusterCounts.put("cluster_" + i, (int) count);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取聚类计数");
        result.put("data", clusterCounts);

        return result;
    }

    /**
     * 获取特定聚类的用户列表
     */
    @GetMapping("/{clusterId}/users")
    public Map<String, Object> getClusterUsers(@PathVariable Integer clusterId) {
        List<UserProfile> clusterProfiles = userProfileRepository.findAll().stream()
                .filter(p -> p.getClusterId() == clusterId)
                .toList();

        Map<String, Object> result = new HashMap<>();

        if (clusterProfiles.isEmpty()) {
            result.put("code", 404);
            result.put("message", "聚类不存在或无用户");
            return result;
        }

        // 获取用户详细信息
        List<Map<String, Object>> userDetails = clusterProfiles.stream()
                .map(profile -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    CampusUser user = userRepository.findById(profile.getUserId()).orElse(null);

                    if (user != null) {
                        userInfo.put("userId", user.getUserId());
                        userInfo.put("name", user.getName());
                        userInfo.put("studentId", user.getStudentId());
                        userInfo.put("college", user.getCollege());
                        userInfo.put("major", user.getMajor());
                        userInfo.put("tags", profile.getTags());
                    }

                    return userInfo;
                })
                .toList();

        result.put("code", 200);
        result.put("message", "成功获取聚类用户列表");
        result.put("data", userDetails);
        result.put("count", userDetails.size());
        result.put("clusterId", clusterId);

        return result;
    }

    /**
     * 获取聚类详情（包含统计信息）
     */
    @GetMapping("/{clusterId}/detail")
    public Map<String, Object> getClusterDetail(@PathVariable Integer clusterId) {
        List<UserProfile> clusterProfiles = userProfileRepository.findAll().stream()
                .filter(p -> p.getClusterId() == clusterId)
                .toList();

        Map<String, Object> detail = new HashMap<>();
        detail.put("clusterId", clusterId);
        detail.put("userCount", clusterProfiles.size());
        detail.put("tags", clusterProfiles.stream()
                .map(UserProfile::getTags)
                .toList());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取聚类详情");
        result.put("data", detail);

        return result;
    }

    /**
     * 获取所有聚类的分布
     */
    @GetMapping("/distribution")
    public Map<String, Object> getClusterDistribution() {
        List<Map<String, Object>> distribution = new java.util.ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final int clusterId = i;
            long count = userProfileRepository.findAll().stream()
                    .filter(p -> p.getClusterId() == clusterId)
                    .count();

            Map<String, Object> clusterInfo = new HashMap<>();
            clusterInfo.put("clusterId", clusterId);
            clusterInfo.put("count", count);

            // 根据聚类ID生成标签
            String clusterName = getClusterName(clusterId);
            clusterInfo.put("name", clusterName);

            distribution.add(clusterInfo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取聚类分布");
        result.put("data", distribution);

        return result;
    }

    /**
     * 根据聚类ID获取聚类名称
     */
    private String getClusterName(int clusterId) {
        switch (clusterId) {
            case 0:
                return "学霸型";
            case 1:
                return "平衡型";
            case 2:
                return "娱乐型";
            case 3:
                return "宅家型";
            case 4:
                return "偏差型";
            default:
                return "未知聚类";
        }
    }
}
