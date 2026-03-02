package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.campus.entity.AnalysisData;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.repository.UserProfileRepository;
import com.example.bysjdesign.repository.AnalysisDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campus/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final CampusUserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AnalysisDataRepository analysisDataRepository;

    public UserController(CampusUserRepository userRepository,
                          UserProfileRepository userProfileRepository,
                          AnalysisDataRepository analysisDataRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.analysisDataRepository = analysisDataRepository;
    }

    @GetMapping
    public Map<String, Object> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<CampusUser> users = userRepository.findAll(PageRequest.of(page, size)).getContent();
        logger.info("API [/api/campus/users] 获取到用户数量: {}", users.size());

        List<Map<String, Object>> enrichedUsers = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", user.getUserId());
            map.put("studentId", user.getStudentId());
            map.put("name", user.getName());
            map.put("college", user.getCollege());

            UserProfile profile = userProfileRepository.findByUserId(user.getUserId());
            map.put("cluster", profile != null ? profile.getClusterId() : 0);

            Optional<AnalysisData> latest = analysisDataRepository.findFirstByUserIdOrderByAnalysisDateDesc(user.getUserId());
            map.put("healthScore", latest.isPresent() && latest.get().getHealthScore() != null
                    ? latest.get().getHealthScore().intValue()
                    : (60 + (user.getUserId() % 40)));

            map.put("status", user.getStatus());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", enrichedUsers);
        result.put("total", userRepository.count());
        return result;
    }
}