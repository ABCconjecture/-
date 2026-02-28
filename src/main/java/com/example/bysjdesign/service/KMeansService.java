package com.example.bysjdesign.service;

import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.repository.UserProfileRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import java.util.*;

@Service
public class KMeansService {

    @Autowired
    private UserProfileRepository profileRepository;

    /**
     * 执行K-means聚类并保存画像（可被定时任务或事件触发）
     */
    public void clusterAndSave(Map<Integer, double[]> featureMap, int k) throws Exception {
        if (featureMap.isEmpty()) return;

        // 构建 Instances
        ArrayList<Attribute> atts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            atts.add(new Attribute("attr" + i));
        }
        Instances instances = new Instances("user_features", atts, featureMap.size());

        List<Integer> userIds = new ArrayList<>();
        for (Map.Entry<Integer, double[]> entry : featureMap.entrySet()) {
            userIds.add(entry.getKey());
            Instance inst = new DenseInstance(1.0, entry.getValue());
            instances.add(inst);
        }

        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(k);
        kmeans.setSeed(42);
        kmeans.buildClusterer(instances);

        for (int i = 0; i < userIds.size(); i++) {
            int uid = userIds.get(i);
            int clusterId = kmeans.clusterInstance(instances.instance(i));
            double[] features = featureMap.get(uid);
            String tags = generateTags(clusterId, features);
            String featureJson = new Gson().toJson(features);

            UserProfile profile = new UserProfile();
            profile.setUserId(uid);
            profile.setClusterId(clusterId);
            profile.setTags(tags);
            profile.setFeatureVector(featureJson);
            profile.setUpdateTime(new Date());
            profileRepository.save(profile);
        }
    }

    private String generateTags(int clusterId, double[] features) {
        Map<String, Object> tags = new HashMap<>();
        tags.put("cluster", clusterId);
        if (features[1] > 0.6) {
            tags.put("type", "勤奋好学");
        } else if (features[2] > 0.5) {
            tags.put("type", "休闲社交");
        } else if (features[4] > 0.3) {
            tags.put("type", "夜猫子");
        } else {
            tags.put("type", "普通学生");
        }
        tags.put("study_score", features[1]);
        tags.put("leisure_score", features[2]);
        tags.put("night_ratio", features[4]);
        return new Gson().toJson(tags);
    }

    public void performClustering() {
    }

    public Map<Integer, Integer> getUserClusterMap() {
        return null;
    }
}