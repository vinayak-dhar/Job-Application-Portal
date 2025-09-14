package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/employer")
public class EmployerController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // 1. Create Job
    @PostMapping("/create-job")
    public String createJob(@RequestBody Job job, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "Please login first!";
        }
        if (!"EMPLOYER".equals(loggedIn.getRole())) {
            return "Access Denied! Only Employers can create jobs.";
        }

        job.setEmployer(loggedIn);
        jobService.createJob(job);
        return "Job created successfully by " + loggedIn.getName();
    }

    // 2. View applicants for a job
    @GetMapping("/job/{jobId}/applicants")
    public List<JobApplication> getApplicants(@PathVariable Long jobId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access denied!");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        // Only job owner can view applicants
        if (!job.getEmployer().getId().equals(loggedIn.getId())) {
            return (List<JobApplication>) ResponseEntity.status(403).body("You can only view applicants for your own jobs!");
        }

        List<JobApplication> applicants = applicationRepository.findByJob(job);

        if (applicants == null || applicants.isEmpty()) {
            return (List<JobApplication>) ResponseEntity.ok(Collections.emptyList()); // return []
        }

        return ResponseEntity.ok(applicants).getBody();
    }

    // 3. Update application status
    @PutMapping("/application/{applicationId}/status")
    public JobApplication updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam String status,
            HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access denied!");
        }

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        if (!application.getJob().getEmployer().getId().equals(loggedIn.getId())) {
            throw new RuntimeException("You can only update applications for your own jobs!");
        }

        if (!status.equals("APPROVED") && !status.equals("REJECTED") && !status.equals("PENDING")) {
            throw new RuntimeException("Invalid status! Must be PENDING, APPROVED, or REJECTED.");
        }

        application.setStatus(status);
        return applicationRepository.save(application);
    }

    // 4. Delete a job
    @DeleteMapping("/delete-job/{jobId}")
    public String deleteJob(@PathVariable Long jobId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "Please login first!";
        }
        if (!"EMPLOYER".equals(loggedIn.getRole())) {
            return "Access Denied! Only Employers can delete jobs.";
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        // Ensure employer owns this job
        if (!job.getEmployer().getId().equals(loggedIn.getId())) {
            return "You can only delete jobs that you created!";
        }

        jobRepository.delete(job);
        return "Job with ID " + jobId + " deleted successfully!";
    }
}
