package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Repository.UserRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

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

    // ------------------ Thymeleaf Views ------------------

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) return "redirect:/login";
        return "admin-dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers(@RequestParam(required = false) String role, Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) return "redirect:/login";

        List<User> users = (role != null)
                ? userRepository.findAll().stream().filter(u -> u.getRole().equalsIgnoreCase(role)).toList()
                : userRepository.findAll();

        model.addAttribute("users", users);
        return "manage-users";
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUserView(@PathVariable Long id, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) return "redirect:/login";

        userRepository.deleteById(id);
        return "redirect:/admin/manage-users";
    }

    @GetMapping("/monitor-jobs")
    public String monitorJobs(Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) return "redirect:/login";

        model.addAttribute("jobs", jobRepository.findAll());
        return "monitor-jobs";
    }

    @GetMapping("/monitor-applications")
    public String monitorApplications(Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) return "redirect:/login";

        model.addAttribute("applications", applicationRepository.findAll());
        return "monitor-applications";
    }
}
