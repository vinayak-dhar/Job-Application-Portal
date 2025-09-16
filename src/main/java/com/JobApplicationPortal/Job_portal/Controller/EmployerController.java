package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // ------------------ Thymeleaf Views ------------------

    @GetMapping("/dashboard")
    public String employerDashboard(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) return "redirect:/login";
        return "employer-dashboard";
    }

    @GetMapping("/create-job")
    public String createJobForm(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) return "redirect:/login";
        return "create-job";
    }

    @PostMapping("/create-job")
    public String createJobSubmit(@ModelAttribute Job job, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) return "redirect:/login";

        job.setEmployer(loggedIn);
        jobRepository.save(job);
        return "redirect:/employer/my-jobs";
    }

    @GetMapping("/my-jobs")
    public String myJobs(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) return "redirect:/login";

        List<Job> jobs = jobRepository.findAll().stream()
                .filter(j -> j.getEmployer().getId().equals(loggedIn.getId()))
                .toList();
        model.addAttribute("jobs", jobs);
        return "my-jobs";
    }

    @GetMapping("/job/{jobId}/applicants")
    public String viewApplicants(@PathVariable Long jobId, HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) return "redirect:/login";

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (!job.getEmployer().getId().equals(loggedIn.getId())) return "redirect:/employer/my-jobs";

        List<JobApplication> applicants = applicationRepository.findByJob(job);
        model.addAttribute("job", job);
        model.addAttribute("applicants", applicants);
        return "applicants";
    }

    // Update applicant status
    @PostMapping("/application/{id}/update-status")
    public String updateApplicantStatusView(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session) {

        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) return "redirect:/login";

        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        if (!application.getJob().getEmployer().getId().equals(loggedIn.getId())) {
            return "redirect:/employer/my-jobs";
        }

        application.setStatus(status);
        applicationRepository.save(application);

        return "redirect:/employer/job/" + application.getJob().getId() + "/applicants";
    }

    // ------------------ REST Endpoints ------------------

    @ResponseBody
    @PostMapping("/api/create-job")
    public String createJob(@RequestBody Job job, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) return "Please login first!";
        if (!"EMPLOYER".equals(loggedIn.getRole())) return "Access Denied! Only Employers can create jobs.";

        job.setEmployer(loggedIn);
        jobService.createJob(job);
        return "Job created successfully by " + loggedIn.getName();
    }

    @ResponseBody
    @GetMapping("/api/job/{jobId}/applicants")
    public List<JobApplication> getApplicants(@PathVariable Long jobId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) throw new RuntimeException("Access denied!");

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (!job.getEmployer().getId().equals(loggedIn.getId())) {
            throw new RuntimeException("You can only view applicants for your own jobs!");
        }

        return applicationRepository.findByJob(job);
    }

    @ResponseBody
    @PutMapping("/api/application/{applicationId}/status")
    public JobApplication updateApplicationStatusApi(
            @PathVariable Long applicationId,
            @RequestParam String status,
            HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) throw new RuntimeException("Access denied!");

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

    @ResponseBody
    @DeleteMapping("/api/delete-job/{jobId}")
    public String deleteJob(@PathVariable Long jobId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) return "Please login first!";
        if (!"EMPLOYER".equals(loggedIn.getRole())) return "Access Denied! Only Employers can delete jobs.";

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (!job.getEmployer().getId().equals(loggedIn.getId())) {
            return "You can only delete jobs that you created!";
        }

        jobRepository.delete(job);
        return "Job with ID " + jobId + " deleted successfully!";
    }
}
