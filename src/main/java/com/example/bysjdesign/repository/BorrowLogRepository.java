package com.example.bysjdesign.repository;

import com.example.bysjdesign.campus.entity.BorrowLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowLogRepository extends JpaRepository<BorrowLog, Long> {
    // 修正：Integer -> Long
    List<BorrowLog> findByUserIdAndBorrowDateAfter(Integer userId, LocalDate startDate);
}