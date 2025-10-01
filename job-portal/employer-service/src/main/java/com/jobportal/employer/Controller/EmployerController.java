package com.jobportal.employer.Controller;

import com.jobportal.employer.Entity.Job;
import com.jobportal.employer.Entity.JobApplication;
import com.jobportal.employer.Entity.User;
import com.jobportal.employer.Repository.JobApplicationRepository;
import com.jobportal.employer.Repository.JobRepository;
import com.jobportal.employer.Repository.UserRepository;
import com.jobportal.employer.Service.JobService;
import jakarta.servlet.http.HttpSession;
import org.apache.juli.logging.Log;
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
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    private static final String AUTH_LOGIN_URL = "redirect:http://localhost:8081/login";

    // ------------------ Thymeleaf Views ------------------

    @GetMapping("/dashboard")
    public String employerDashboard(@RequestParam(required = false) Long userId, HttpSession httpSession) {
        if (userId != null) httpSession.setAttribute("userId", userId);
        if (httpSession.getAttribute("userId") == null) return AUTH_LOGIN_URL;
        return "employer-dashboard";
    }

    @GetMapping("/create-job")
    public String createJobForm(HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) return AUTH_LOGIN_URL;
        return "create-job";
    }

    @PostMapping("/create-job")
    public String createJobSubmit(@ModelAttribute Job job,
                                  HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) return AUTH_LOGIN_URL;

        // Set the employer
        User employer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        job.setEmployer(employer);
        jobRepository.save(job);
        return "redirect:/employer/my-jobs";
    }

    @GetMapping("/my-jobs")
    public String myJobs(HttpSession httpSession, Model model) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) return AUTH_LOGIN_URL;

        // Fetch jobs posted by this employer
        User employer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        List<Job> jobs = jobRepository.findByEmployer(employer);
        model.addAttribute("jobs", jobs);
        return "my-jobs";
    }


    @GetMapping("/job/{jobId}/applicants")
    public String viewApplicants(@PathVariable Long jobId,
                                 HttpSession httpSession,
                                 Model model) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) return AUTH_LOGIN_URL;

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
                                            HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) return AUTH_LOGIN_URL;

        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        application.setStatus(status);
        applicationRepository.save(application);

        return "redirect:/employer/job/" + application.getJob().getId() + "/applicants";
    }

    @PostMapping("/delete-job/{id}")
    public String deleteJob(@PathVariable Long id,
                            HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) return AUTH_LOGIN_URL;

        jobRepository.deleteById(id);
        return "redirect:/employer/my-jobs";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return AUTH_LOGIN_URL;
    }
}
