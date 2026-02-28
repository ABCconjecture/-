package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    // ✅ 已修正：Integer -> Long
    List<AccessLog> findByUserIdAndEntryTimeBetween(Long userId, Date start, Date end);

    @Query("SELECT a.userId, COUNT(a) as count " +
            "FROM AccessLog a WHERE a.locationType='教学楼' AND a.entryTime BETWEEN :start AND :end GROUP BY a.userId")
    List<Object[]> classroomSummary(@Param("start") Date start, @Param("end") Date end);

    @Query("SELECT a.userId, COUNT(a) as count " +
            "FROM AccessLog a WHERE a.locationType='宿舍' AND HOUR(a.entryTime) >= 23 " +
            "AND a.entryTime BETWEEN :start AND :end GROUP BY a.userId")
    List<Object[]> lateReturnSummary(@Param("start") Date start, @Param("end") Date end);

    // ✅ 已修正：Integer -> Long (解决 156行附近报错的关键)
    List<AccessLog> findByUserIdAndEntryTimeAfter(Long userId, LocalDateTime atStartOfDay);

    List<AccessLog> findByUserId(Long userId);
}