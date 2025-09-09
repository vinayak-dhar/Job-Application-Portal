package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // 1. View all available jobs
    @GetMapping("/jobs")
    public List<Job> viewJobs(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            throw new RuntimeException("Please login first!");
        }
        if (!"EMPLOYEE".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access Denied! Only Employees can view jobs.");
        }
        return jobRepository.findAll();
    }

    // 2. Apply for a job
    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "Please login first!";
        }
        if (!"EMPLOYEE".equals(loggedIn.getRole())) {
            return "Access Denied! Only Employees can apply for jobs.";
        }

        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setEmployeeEmail(loggedIn.getEmail());
        applicationRepository.save(application);

        return "Applied for Job ID: " + jobId + " successfully!";
    }

    // 3. Check application status
    @GetMapping("/applications")
    public List<JobApplication> viewMyApplications(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            throw new RuntimeException("Please login first!");
        }
        if (!"EMPLOYEE".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access Denied! Only Employees can check applications.");
        }

        return applicationRepository.findByEmployeeEmail(loggedIn.getEmail());
    }
}

