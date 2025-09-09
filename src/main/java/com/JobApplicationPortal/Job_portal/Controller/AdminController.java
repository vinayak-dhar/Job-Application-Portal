package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Entity.User;
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
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // 1. View all job applications
    @GetMapping("/applications")
    public List<JobApplication> getAllApplications(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            throw new RuntimeException("Please login first!");
        }
        if (!"ADMIN".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access Denied! Only Admins can view all applications.");
        }
        return applicationRepository.findAll();
    }

    // 2. View all posted jobs
    @GetMapping("/jobs")
    public List<Job> getAllJobs(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            throw new RuntimeException("Please login first!");
        }
        if (!"ADMIN".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access Denied! Only Admins can view all jobs.");
        }
        return jobRepository.findAll();
    }
}
