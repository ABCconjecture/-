package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.NetworkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface NetworkLogRepository extends JpaRepository<NetworkLog, Long> {
    // ✅ 已修正：Integer -> Long
    List<NetworkLog> findByUserIdAndSessionStartBetween(Long userId, Date start, Date end);

    @Query(value = "SELECT l.user_id, SUM(l.duration_sec) as totalDuration, " +
            "SUM(CASE WHEN l.category='休闲' THEN l.download_bytes + l.upload_bytes ELSE 0 END) as leisureTraffic, " +
            "SUM(l.download_bytes + l.upload_bytes) as totalTraffic " +
            "FROM network_log l WHERE l.session_start BETWEEN ?1 AND ?2 GROUP BY l.user_id", nativeQuery = true)
    List<Object[]> summaryByUser(Date start, Date end);

    // ✅ 已修正：Integer -> Long (解决报错的关键)
    List<NetworkLog> findByUserIdAndSessionStartAfter(Long userId, LocalDateTime atStartOfDay);

    List<NetworkLog> findByUserId(Long userId);
}