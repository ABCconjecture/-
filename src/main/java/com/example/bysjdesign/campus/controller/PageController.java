package com.example.bysjdesign.campus.controller;

import com.example.bysjdesign.campus.entity.CampusUser;
import com.example.bysjdesign.campus.entity.UserProfile;
import com.example.bysjdesign.repository.CampusUserRepository;
import com.example.bysjdesign.repository.UserProfileRepository;
import com.example.bysjdesign.repository.WarningLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/campus")
public class PageController {

    @Autowired
    private CampusUserRepository userRepository;
    @Autowired
    private UserProfileRepository profileRepository;
    @Autowired
    private WarningLogRepository warningLogRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            session.setAttribute("user", "admin");
        }
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("unhandledWarnings", warningLogRepository.countByStatus(0));
        return "campus/dashboard";
    }

    @GetMapping("/users")
    public String users() {
        return "campus/users";
    }

    @GetMapping("/profile/{userId}")
    public String profile(@PathVariable Integer userId, Model model) {
        CampusUser user = userRepository.findById(userId).orElse(null);
        UserProfile profile = profileRepository.findByUserId(userId);
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "campus/profile";
    }

    @GetMapping("/warning")
    public String warning(Model model) {
        model.addAttribute("warnings", warningLogRepository.findByStatus(0));
        return "campus/warning";
    }

    @GetMapping("/cluster/{clusterId}")
    public String clusterDetail(@PathVariable Integer clusterId, Model model) {
        model.addAttribute("clusterId", clusterId);
        return "campus/cluster";
    }

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/campus/dashboard";
    }
}