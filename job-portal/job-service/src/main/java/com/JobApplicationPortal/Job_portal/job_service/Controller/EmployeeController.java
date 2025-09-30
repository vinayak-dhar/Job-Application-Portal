package com.JobApplicationPortal.Job_portal.job_service.Controller;

import com.JobApplicationPortal.Job_portal.job_service.Entity.Job;
import com.JobApplicationPortal.Job_portal.job_service.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.job_service.Entity.User;
import com.JobApplicationPortal.Job_portal.job_service.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.job_service.Repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private RestTemplate restTemplate;
    private final String AUTH_SERVICE_URL = "http://auth-service";

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    private boolean isEmployee(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && role.equalsIgnoreCase("EMPLOYEE");
    }

    private User getLoggedInUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        if (email == null || token == null) return null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                AUTH_SERVICE_URL + "/auth/user?email=" + email,
                HttpMethod.GET,
                entity,
                User.class
        ).getBody();
    }

    // ---------------------------
    // Thymeleaf Endpoints
    // ---------------------------

    // Dashboard (View)
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (!isEmployee(session)) return "redirect:/login";
        return "employee-dashboard";
    }

    // View Jobs (View)
    @GetMapping("/jobs")
    public String viewJobs(Model model, HttpSession session) {
        if (!isEmployee(session)) return "redirect:/login";
        model.addAttribute("jobs", jobRepository.findAll());
        return "employee-jobs";
    }

    // Apply for Job (View)
    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId, HttpSession session) {
        if (!isEmployee(session)) return "redirect:/login";

        User loggedIn = getLoggedInUser(session);
        if (loggedIn == null) return "redirect:/login";

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
        if (!isEmployee(session)) return "redirect:/login";

        User loggedIn = getLoggedInUser(session);
        if (loggedIn == null) return "redirect:/login";

        // Fetch applications for this employee
        List<JobApplication> applications = applicationRepository.findByEmployee(loggedIn);
        model.addAttribute("applications", applications);
        return "employee-applications";
    }

    // Withdraw Application (View)
    @PostMapping("/applications/{applicationId}/withdraw")
    public String withdrawApplication(@PathVariable Long applicationId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!isEmployee(session)) return "redirect:/login";

        User loggedIn = getLoggedInUser(session);
        if (loggedIn == null) return "redirect:/login";

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        // Ensure the logged-in employee owns this application
        if (!application.getEmployee().getId().equals(loggedIn.getId())) {
            redirectAttributes.addFlashAttribute("error", "You cannot withdraw someone else's application.");
            return "redirect:/employee/applications";
        }

        // Only allow withdrawn if not already withdrawn
        if ("WITHDRAWN".equals(application.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This application is already withdrawn.");
            return "redirect:/employee/applications";
        }

        application.setStatus("WITHDRAWN");
        applicationRepository.save(application);

        redirectAttributes.addFlashAttribute("success", "Application withdrawn successfully!");
        return "redirect:/employee/applications";
    }
}
