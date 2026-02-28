package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    UserProfile findByUserId(Integer userId);
    List<UserProfile> findByClusterId(Integer clusterId);
    @Query("SELECT p.clusterId, COUNT(p) FROM UserProfile p GROUP BY p.clusterId")
    List<Object[]> countByCluster();
}