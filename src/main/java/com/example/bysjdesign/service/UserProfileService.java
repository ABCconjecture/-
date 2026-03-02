package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.repository.UserProfileRepository;
import com.example.bysjdesign.repository.CampusUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Service
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private CampusUserRepository campusUserRepository;
    @Autowired private KMeansService kMeansService;

    /**
     * ✅ 纠正：补全定时任务调用的核心方法，实现从聚类到画像的闭环
     */
    public void generateUserProfiles() {
        logger.info("开始执行用户画像批量更新...");
        try {
            List<CampusUser> users = campusUserRepository.findAll();
            // 获取 KMeansService 计算出的聚类映射 [userId -> clusterId]
            Map<Integer, Integer> clusterMap = kMeansService.getUserClusterMap();

            if (clusterMap == null || clusterMap.isEmpty()) {
                logger.warn("聚类映射为空，请检查 KMeansService 逻辑");
                return;
            }

            for (CampusUser user : users) {
                Integer clusterId = clusterMap.get(user.getUserId());
                if (clusterId != null) {
                    updateSingleProfile(user.getUserId(), clusterId);
                }
            }
        } catch (Exception e) {
            logger.error("画像生成异常: ", e);
        }
    }

    private void updateSingleProfile(Integer userId, Integer clusterId) {
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }
        profile.setClusterId(clusterId);
        profile.setTags(mapClusterToTags(clusterId));
        profile.setUpdateTime(new Date());
        userProfileRepository.save(profile);
    }

    private String mapClusterToTags(Integer clusterId) {
        return switch (clusterId) {
            case 0 -> "学术型,图书馆常客,规律作息";
            case 1 -> "活跃型,高社交,网络活跃";
            case 2 -> "预警型,熬夜,学习投入不足";
            default -> "普通学生,均衡发展";
        };
    }
}