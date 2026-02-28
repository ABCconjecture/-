package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户API控制器
 * 提供用户列表、用户详情等REST接口
 */
@RestController
@RequestMapping("/api/campus/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private CampusUserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public Map<String, Object> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<CampusUser> users = userRepository.findAll(pageable).getContent();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功获取用户列表");
        result.put("data", users);
        result.put("page", page);
        result.put("size", size);
        result.put("total", userRepository.count());

        return result;
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getUserDetail(@PathVariable Integer userId) {
        CampusUser user = userRepository.findById(userId).orElse(null);

        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }

        result.put("code", 200);
        result.put("message", "成功获取用户详情");
        result.put("data", user);

        return result;
    }

    /**
     * 获取用户画像
     */
    @GetMapping("/{userId}/profile")
    public Map<String, Object> getUserProfile(@PathVariable Integer userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId);

        Map<String, Object> result = new HashMap<>();

        if (profile == null) {
            result.put("code", 404);
            result.put("message", "用户画像不存在");
            return result;
        }

        result.put("code", 200);
        result.put("message", "成功获取用户画像");
        result.put("data", profile);

        return result;
    }

    /**
     * 搜索用户（学号或姓名）
     */
    @GetMapping("/search")
    public Map<String, Object> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<CampusUser> users = userRepository.findAll().stream()
                .filter(u -> u.getStudentId().contains(keyword) || u.getName().contains(keyword))
                .skip((long) page * size)
                .limit(size)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "搜索成功");
        result.put("data", users);
        result.put("page", page);
        result.put("size", size);

        return result;
    }
}
