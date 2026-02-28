package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.BorrowLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface BorrowLogRepository extends JpaRepository<BorrowLog, Long> {
    // ✅ 已修正：Integer -> Long
    List<BorrowLog> findByUserIdAndBorrowDateBetween(Long userId, Date start, Date end);

    // ✅ 已修正：Integer -> Long (解决报错的关键)
    List<BorrowLog> findByUserIdAndBorrowDateAfter(Long userId, LocalDate startDate);

    List<BorrowLog> findByUserId(Long userId);
}