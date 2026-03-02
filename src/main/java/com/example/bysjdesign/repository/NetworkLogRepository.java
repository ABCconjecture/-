package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.NetworkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface NetworkLogRepository extends JpaRepository<NetworkLog, Long> {
    // ✅ 已修正：参数类型统一为 Long
    List<NetworkLog> findByUserIdAndSessionStartAfter(Integer userId, LocalDateTime atStartOfDay);

    @Query(value = "SELECT l.user_id, SUM(l.duration_sec), SUM(CASE WHEN l.category='休闲' THEN l.download_bytes + l.upload_bytes ELSE 0 END), SUM(l.download_bytes + l.upload_bytes) FROM network_log l WHERE l.session_start BETWEEN ?1 AND ?2 GROUP BY l.user_id", nativeQuery = true)
    List<Object[]> summaryByUser(Date start, Date end);
}