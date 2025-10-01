package com.jobportal.employer.Controller;

import com.jobportal.employer.Entity.Job;
import com.jobportal.employer.Entity.JobApplication;
import com.jobportal.employer.Repository.JobApplicationRepository;
import com.jobportal.employer.Repository.JobRepository;
import com.jobportal.employer.Service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

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

    private static final String AUTH_LOGIN_URL = "redirect:http://localhost:8081/login";

    // ------------------ Thymeleaf Views ------------------

    @GetMapping("/dashboard")
    public String employerDashboard(@RequestParam(required = false) String sessionId, HttpSession session) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;
        return "employer-dashboard";
    }

    @GetMapping("/create-job")
    public String createJobForm(@RequestParam(required = false) String sessionId, HttpSession session) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;
        return "create-job";
    }

    @PostMapping("/create-job")
    public String createJobSubmit(@ModelAttribute Job job,
                                  @RequestParam(required = false) String sessionId,
                                  HttpSession session) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        // The employer is inferred from sessionId in your system; set it accordingly
        // Example: job.setEmployerBySessionId(sessionId);
        jobRepository.save(job);
        return "redirect:/employer/my-jobs";
    }

    @GetMapping("/my-jobs")
    public String myJobs(@RequestParam(required = false) String sessionId, HttpSession session, Model model) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        // Fetch jobs according to sessionId mapping (auth-service provides employer info)
        List<Job> jobs = jobRepository.findAll(); // Adjust with filtering if needed
        model.addAttribute("jobs", jobs);
        return "my-jobs";
    }

    @GetMapping("/job/{jobId}/applicants")
    public String viewApplicants(@PathVariable Long jobId,
                                 @RequestParam(required = false) String sessionId,
                                 HttpSession session,
                                 Model model) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));
        List<JobApplication> applicants = applicationRepository.findByJob(job);

        model.addAttribute("job", job);
        model.addAttribute("applicants", applicants);
        return "applicants";
    }

    @PostMapping("/application/{id}/update-status")
    public String updateApplicantStatusView(@PathVariable Long id,
                                            @RequestParam String status,
                                            @RequestParam(required = false) String sessionId,
                                            HttpSession session) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        application.setStatus(status);
        applicationRepository.save(application);

        return "redirect:/employer/job/" + application.getJob().getId() + "/applicants";
    }

    @PostMapping("/delete-job/{id}")
    public String deleteJob(@PathVariable Long id,
                            @RequestParam(required = false) String sessionId,
                            HttpSession session) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        jobRepository.deleteById(id);
        return "redirect:/employer/my-jobs";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return AUTH_LOGIN_URL;
    }
}
