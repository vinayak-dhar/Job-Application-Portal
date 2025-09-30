package com.JobApplicationPortal.Job_portal.job_service.Controller;

import com.JobApplicationPortal.Job_portal.job_service.Entity.User;
import com.JobApplicationPortal.Job_portal.job_service.Entity.Job;
import com.JobApplicationPortal.Job_portal.job_service.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.job_service.Repository.UserRepository;
import com.JobApplicationPortal.Job_portal.job_service.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.job_service.Repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RestTemplate restTemplate;
    private final String AUTH_SERVICE_URL = "http://auth-service";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // ---------------- Helper Methods ----------------

    private User getLoggedInAdmin(HttpSession session) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        if (email == null || token == null) return null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        User user = restTemplate.exchange(
                AUTH_SERVICE_URL + "/auth/user?email=" + email,
                HttpMethod.GET,
                entity,
                User.class
        ).getBody();

        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            return user;
        }
        return null;
    }

    private boolean isAdmin(HttpSession session) {
        return getLoggedInAdmin(session) != null;
    }

    // ------------------ Thymeleaf Views ------------------

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        return "admin-dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers(@RequestParam(required = false) String role, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        List<User> users;
        if (role != null && !role.isBlank()) {
            users = userRepository.findAll().stream()
                    .filter(u -> u.getRole().equalsIgnoreCase(role))
                    .toList();
        }
        else {
            users = userRepository.findAll();
        }

        model.addAttribute("users", users);
        return "manage-users";
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUserView(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        userRepository.deleteById(id);
        return "redirect:/admin/manage-users";
    }

    @GetMapping("/monitor-jobs")
    public String monitorJobs(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("jobs", jobRepository.findAll());
        return "monitor-jobs";
    }

    @GetMapping("/monitor-applications")
    public String monitorApplications(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("applications", applicationRepository.findAll());
        return "monitor-applications";
    }
}
