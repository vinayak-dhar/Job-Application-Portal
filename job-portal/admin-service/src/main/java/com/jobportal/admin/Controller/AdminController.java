package com.jobportal.admin.Controller;

import com.jobportal.admin.Entity.User;
import com.jobportal.admin.Entity.Job;
import com.jobportal.admin.Entity.JobApplication;
import com.jobportal.admin.Repository.UserRepository;
import com.jobportal.admin.Repository.JobRepository;
import com.jobportal.admin.Repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // Auth-service signup URL
    private static final String AUTH_SIGNUP_URL = "redirect:http://localhost:8081/signup";

    // First entry point from auth-service
    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(required = false) Long userId,
                                 HttpSession httpSession) {
        if (userId != null) {
            // Set session if coming from auth-service
            httpSession.setAttribute("userId", userId);
        }

        // Check session
        if (httpSession.getAttribute("userId") == null) {
            return AUTH_SIGNUP_URL;
        }

        return "admin-dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers(@RequestParam(required = false) String role,
                              HttpSession httpSession,
                              Model model) {
        if (httpSession.getAttribute("userId") == null) {
            return AUTH_SIGNUP_URL;
        }

        List<User> users = (role != null)
                ? userRepository.findAll().stream().filter(u -> u.getRole().equalsIgnoreCase(role)).toList()
                : null;

        model.addAttribute("users", users);
        return "manage-users";
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id,
                             HttpSession httpSession) {
        if (httpSession.getAttribute("userId") == null) {
            return AUTH_SIGNUP_URL;
        }

        userRepository.deleteById(id);
        return "redirect:/admin/manage-users";
    }

    @GetMapping("/monitor-jobs")
    public String monitorJobs(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("userId") == null) {
            return AUTH_SIGNUP_URL;
        }

        model.addAttribute("jobs", jobRepository.findAll());
        return "monitor-jobs";
    }

    @GetMapping("/monitor-applications")
    public String monitorApplications(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("userId") == null) {
            return AUTH_SIGNUP_URL;
        }

        model.addAttribute("applications", applicationRepository.findAll());
        return "monitor-applications";
    }

    // Logout -> clear session and redirect to auth-service signup
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return AUTH_SIGNUP_URL;
    }
}
