package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employer")
public class EmployerController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // 1. Create Job (Only Employer can access)
    @PostMapping("/create-job")
    public String createJob(@RequestBody Job job, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "Please login first!";
        }
        if (!"EMPLOYER".equals(loggedIn.getRole())) {
            return "Access Denied! Only Employers can create jobs.";
        }

        // Set employer info in job
        job.setEmployerEmail(loggedIn.getEmail());
        jobService.createJob(job);
        return "Job created successfully by " + loggedIn.getName();
    }

    // 2. View all applicants for employer's jobs
    @GetMapping("/applicants")
    public List<JobApplication> viewApplicants(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            throw new RuntimeException("Please login first!");
        }
        if (!"EMPLOYER".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access Denied! Only Employers can view applicants.");
        }

        // Find jobs created by this employer
        List<Job> jobs = jobRepository.findAll()
                .stream()
                .filter(job -> job.getEmployerEmail().equals(loggedIn.getEmail()))
                .toList();

        // Get jobIds
        List<Long> jobIds = jobs.stream().map(Job::getId).toList();

        // Find applications for those jobs
        return applicationRepository.findAll()
                .stream()
                .filter(app -> jobIds.contains(app.getJobId()))
                .collect(Collectors.toList());
    }

    // 3. Update application status (Approve/Reject)
    @PutMapping("/applications/{applicationId}/status")
    public String updateApplicationStatus(@PathVariable Long applicationId,
                                          @RequestParam String status,
                                          HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "Please login first!";
        }
        if (!"EMPLOYER".equals(loggedIn.getRole())) {
            return "Access Denied! Only Employers can update applications.";
        }

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        // Check if this application belongs to employer's job
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (!job.getEmployerEmail().equals(loggedIn.getEmail())) {
            return "Access Denied! You can only update applications for your own jobs.";
        }

        if (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED")) {
            return "Invalid status! Use APPROVED or REJECTED.";
        }

        application.setStatus(status.toUpperCase());
        applicationRepository.save(application);

        return "Application status updated to " + status;
    }
}
