package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.repository.UserProfileRepository;
import com.example.bysjdesign.repository.CampusUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户画像服务
 * 根据K-means聚类结果生成用户画像
 */
@Service
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CampusUserRepository campusUserRepository;

    @Autowired
    private KMeansService kMeansService;

    /**
     * 为所有用户生成画像
     */
    public void generateUserProfiles() {
        logger.info("开始生成用户画像...");

        try {
            // 获取所有用户
            List<CampusUser> users = campusUserRepository.findAll();

            // 获取K-means聚类结果
            Map<Integer, Integer> userClusterMap = kMeansService.getUserClusterMap();

            int processed = 0;
            for (CampusUser user : users) {
                try {
                    Integer clusterId = userClusterMap.get(user.getUserId());
                    if (clusterId != null) {
                        generateUserProfile(user.getUserId(), clusterId);
                        processed++;
                    }
                } catch (Exception e) {
                    logger.error("生成用户 {} 画像失败", user.getUserId(), e);
                }
            }

            logger.info("用户画像生成完成，共处理 {} 个用户", processed);
        } catch (Exception e) {
            logger.error("生成用户画像异常", e);
            throw new RuntimeException("生成失败", e);
        }
    }

    /**
     * 为单个用户生成画像
     */
    private void generateUserProfile(Integer userId, Integer clusterId) {
        logger.debug("生成用户 {} 的画像，归属聚类: {}", userId, clusterId);

        // 查找或创建用户画像
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(new UserProfile());

        profile.setUserId(userId);
        profile.setClusterId(clusterId);
        profile.setProfileTags(generateProfileTags(clusterId));
        profile.setUpdateTime(new java.util.Date());

        userProfileRepository.save(profile);
    }

    /**
     * 为用户生成标签
     */
    private String generateProfileTags(Integer clusterId) {
        List<String> tags = new ArrayList<>();

        switch (clusterId) {
            case 0:
                tags.addAll(Arrays.asList("学霸", "高学习量", "自律"));
                break;
            case 1:
                tags.addAll(Arrays.asList("均衡", "中等活动", "稳定"));
                break;
            case 2:
                tags.addAll(Arrays.asList("休闲", "低学习量", "娱乐优先"));
                break;
            case 3:
                tags.addAll(Arrays.asList("宅男宅女", "宿舍活动多", "社交少"));
                break;
            case 4:
                tags.addAll(Arrays.asList("运动达人", "健康", "户外活动多"));
                break;
            default:
                tags.add("未分类");
        }

        return String.join(",", tags);
    }

    /**
     * 获取用户画像
     */
    public UserProfile getUserProfile(Integer userId) {
        return userProfileRepository.findByUserId(userId).orElse(null);
    }

    /**
     * 获取聚类内的所有用户
     */
    public List<UserProfile> getClusterUsers(Integer clusterId) {
        return userProfileRepository.findByClusterId(clusterId);
    }

    /**
     * 获取聚类统计
     */
    public Map<Integer, Long> getClusterStatistics() {
        List<UserProfile> profiles = userProfileRepository.findAll();

        return profiles.stream()
                .collect(Collectors.groupingBy(
                        UserProfile::getClusterId,
                        Collectors.counting()
                ));
    }
}