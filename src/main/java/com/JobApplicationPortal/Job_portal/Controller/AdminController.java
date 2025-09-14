package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Repository.UserRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // Manage all users
    @GetMapping("/users")
    public List<User> getAllUsers(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access denied!");
        }
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access denied!");
        }
        userRepository.deleteById(id);
        return "User deleted successfully";
    }

    // Monitor all jobs
    @GetMapping("/jobs")
    public List<Job> getAllJobs(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access denied!");
        }
        return jobRepository.findAll();
    }

    // Monitor all applications
    @GetMapping("/applications")
    public List<JobApplication> getAllApplications(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access denied!");
        }
        return applicationRepository.findAll();
    }
}
