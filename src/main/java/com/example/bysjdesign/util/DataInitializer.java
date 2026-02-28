package com.example.bysjdesign.util;

import com.example.bysjdesign.campus.entity.*;
import com.example.bysjdesign.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CampusUserRepository userRepository;

    @Autowired
    private NetworkLogRepository networkLogRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private BorrowLogRepository borrowLogRepository;

    @Autowired
    private AnalysisDataRepository analysisDataRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            loadUsers();
            loadNetworkLogs();
            loadAccessLogs();
            loadBorrowLogs();
            generateAnalysisData();
        }
    }

    private void loadUsers() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/user.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : parser) {
                CampusUser user = new CampusUser();
                user.setStudentId(record.get("student_id"));
                user.setName(record.get("name"));
                user.setGender(record.get("gender"));
                user.setEnrollmentYear(Integer.parseInt(record.get("enrollment_year")));
                user.setCollege(record.get("college"));
                user.setMajor(record.get("major"));
                user.setClazz(record.get("class"));
                user.setStatus(1);
                user.setCreateTime(new Date());
                userRepository.save(user);
            }
        }
    }

    private void loadNetworkLogs() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/network_log.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : parser) {
                NetworkLog log = new NetworkLog();
                log.setUserId(Integer.parseInt(record.get("user_id")));
                log.setCategory(record.get("category"));
                log.setIsAbnormal(Integer.parseInt(record.get("is_abnormal")));
                networkLogRepository.save(log);
            }
        }
    }

    private void loadAccessLogs() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/access_log.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : parser) {
                AccessLog log = new AccessLog();
                log.setUserId(Integer.parseInt(record.get("user_id")));
                log.setLocationType(record.get("location_type"));
                log.setLocationName(record.get("location_name"));
                accessLogRepository.save(log);
            }
        }
    }

    private void loadBorrowLogs() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/borrow_log.csv");
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : parser) {
                BorrowLog log = new BorrowLog();
                log.setUserId(Integer.parseInt(record.get("user_id")));
                log.setBookTitle(record.get("book_title"));
                log.setCategory(record.get("category"));
                borrowLogRepository.save(log);
            }
        }
    }

    private void generateAnalysisData() throws Exception {
        List<CampusUser> users = userRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (CampusUser user : users) {
            // Check if analysis data already exists for this user
            List<AnalysisData> existing = analysisDataRepository.findByUserId(user.getUserId(),
                org.springframework.data.domain.PageRequest.of(0, 1));

            if (existing.isEmpty()) {
                AnalysisData analysis = new AnalysisData();
                analysis.setUserId(user.getUserId().longValue());
                analysis.setAnalysisDate(LocalDate.now());
                analysis.setCreateTime(LocalDateTime.now());
                analysis.setUpdateTime(LocalDateTime.now());

                // Generate some basic metrics
                Random random = new Random(user.getUserId()); // Use userId as seed for consistency
                analysis.setNetworkActivityCount(random.nextInt(50) + 10);
                analysis.setLibraryAccessCount(random.nextInt(20) + 5);
                analysis.setClassroomAccessCount(random.nextInt(30) + 10);
                analysis.setLateReturnCount(random.nextInt(5));
                analysis.setActiveDays(25);
                analysis.setAvgAccessFrequency(2.5 + random.nextDouble());
                analysis.setBorrowCount((long)(random.nextInt(15) + 5));
                analysis.setAvgBorrowDays(14.0 + random.nextDouble() * 10);
                analysis.setUnreturnedCount(random.nextInt(2));

                // Calculate risk score
                double networkRisk = random.nextDouble() * 30;
                double accessRisk = random.nextDouble() * 20;
                double borrowRisk = random.nextDouble() * 15;
                double riskScore = networkRisk + accessRisk + borrowRisk;

                analysis.setNetworkRisk(networkRisk);
                analysis.setAccessRisk(accessRisk);
                analysis.setBorrowRisk(borrowRisk);
                analysis.setRiskScore(Math.min(riskScore, 100.0));
                analysis.setHealthScore(100.0 - Math.min(riskScore, 100.0));

                analysisDataRepository.save(analysis);
            }
        }
    }
}