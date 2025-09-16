package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // ---------------------------
    // Thymeleaf Endpoints
    // ---------------------------

    // Dashboard (View)
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYEE".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }
        return "employee-dashboard";
    }

    // View Jobs (View)
    @GetMapping("/jobs")
    public String viewJobs(Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYEE".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("jobs", jobRepository.findAll());
        return "employee-jobs";
    }

    // Apply for Job (View)
    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYEE".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (applicationRepository.findByEmployeeAndJob(loggedIn, job).isPresent()) {
            return "redirect:/employee/jobs?error=alreadyApplied";
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setEmployee(loggedIn);
        application.setStatus("PENDING");

        applicationRepository.save(application);

        return "redirect:/employee/applications";
    }

    // View Applications (View)
    @GetMapping("/applications")
    public String viewMyApplications(Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYEE".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }

        List<JobApplication> applications = applicationRepository.findByEmployee(loggedIn);
        model.addAttribute("applications", applications);
        return "employee-applications";
    }

    // Withdraw Application (View)
    @PostMapping("/applications/{applicationId}/withdraw")
    public String withdrawApplication(@PathVariable Long applicationId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        User loggedIn = (User) session.getAttribute("user");

        if (loggedIn == null || !"EMPLOYEE".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        // Ensure the logged-in employee owns this application
        if (!application.getEmployee().getId().equals(loggedIn.getId())) {
            redirectAttributes.addFlashAttribute("error", "You cannot withdraw someone else's application.");
            return "redirect:/employee/applications";
        }

        // Only allow withdraw if not already withdrawn
        if ("WITHDRAWN".equals(application.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This application is already withdrawn.");
            return "redirect:/employee/applications";
        }

        application.setStatus("WITHDRAWN");
        applicationRepository.save(application);

        redirectAttributes.addFlashAttribute("success", "Application withdrawn successfully!");
        return "redirect:/employee/applications";
    }


    // ---------------------------
    // REST API Endpoints (JSON)
    // ---------------------------

    // Get all jobs (JSON)
    @GetMapping("/api/jobs")
    @ResponseBody
    public List<Job> apiViewJobs(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            throw new RuntimeException("Please login first!");
        }
        if (!"EMPLOYEE".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access Denied! Only Employees can view jobs.");
        }
        return jobRepository.findAll();
    }

    // Apply for Job (JSON)
    @PostMapping("/api/apply/{jobId}")
    @ResponseBody
    public String apiApplyForJob(@PathVariable Long jobId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "Please login first!";
        }
        if (!"EMPLOYEE".equals(loggedIn.getRole())) {
            return "Access Denied! Only Employees can apply for jobs.";
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (applicationRepository.findByEmployeeAndJob(loggedIn, job).isPresent()) {
            return "You have already applied for this job!";
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setEmployee(loggedIn);
        application.setStatus("PENDING");

        applicationRepository.save(application);

        return "Applied for Job ID: " + jobId + " successfully!";
    }

    // Get applications (JSON)
    @GetMapping("/api/applications")
    @ResponseBody
    public List<JobApplication> apiViewMyApplications(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            throw new RuntimeException("Please login first!");
        }
        if (!"EMPLOYEE".equals(loggedIn.getRole())) {
            throw new RuntimeException("Access Denied! Only Employees can check applications.");
        }
        return applicationRepository.findByEmployee(loggedIn);
    }

    // Withdraw application (JSON)
    @PutMapping("/api/applications/{applicationId}/withdraw")
    @ResponseBody
    public String apiWithdrawApplication(@PathVariable Long applicationId, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "Please login first!";
        }
        if (!"EMPLOYEE".equals(loggedIn.getRole())) {
            return "Access Denied! Only Employees can withdraw applications.";
        }

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        if (!application.getEmployee().getId().equals(loggedIn.getId())) {
            return "You can only withdraw your own applications!";
        }

        application.setStatus("WITHDRAWN");
        applicationRepository.save(application);

        return "Successfully withdrawn from job application ID: " + applicationId;
    }
}
