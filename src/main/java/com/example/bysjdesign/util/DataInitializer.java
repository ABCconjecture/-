package com.example.bysjdesign.util;

import com.example.bysjdesign.campus.entity.*;
import com.example.bysjdesign.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final CampusUserRepository userRepository;
    private final AnalysisDataRepository analysisDataRepository;
    private final RiskWarningRepository riskWarningRepository;

    public DataInitializer(CampusUserRepository userRepository,
                           AnalysisDataRepository analysisDataRepository,
                           RiskWarningRepository riskWarningRepository) {
        this.userRepository = userRepository;
        this.analysisDataRepository = analysisDataRepository;
        this.riskWarningRepository = riskWarningRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            logger.info("========== 开始初始化演示数据 ==========");
            initializeAllData();
            logger.info("========== 演示数据载入成功 ==========");
        } else {
            logger.info("数据库已有用户数据，跳过初始化。");
        }
    }

    private void initializeAllData() {
        Random random = new Random();
        String[] colleges = {"计算机学院", "数学学院", "电子工程学院", "文学院"};

        for (int i = 1; i <= 50; i++) { // 先初始化50个用户测试
            CampusUser user = new CampusUser();
            user.setStudentId("2023" + String.format("%04d", i));
            user.setName("学生_" + i);
            user.setCollege(colleges[i % 4]);
            user.setMajor("专业_" + (i % 8));
            user.setStatus(1);
            user.setCreateTime(new Date());
            CampusUser savedUser = userRepository.save(user);

            // 初始化对应的分析数据
            AnalysisData data = new AnalysisData();
            data.setUserId(savedUser.getUserId());
            data.setAnalysisDate(LocalDate.now());
            data.setHealthScore(60.0 + (savedUser.getUserId() % 40));
            data.setCreateTime(LocalDateTime.now());
            analysisDataRepository.save(data);
        }
    }
}