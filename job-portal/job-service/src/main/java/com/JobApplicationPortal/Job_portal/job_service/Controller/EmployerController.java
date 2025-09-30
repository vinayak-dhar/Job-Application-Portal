package com.JobApplicationPortal.Job_portal.job_service.Controller;

import com.JobApplicationPortal.Job_portal.job_service.Entity.Job;
import com.JobApplicationPortal.Job_portal.job_service.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.job_service.Entity.User;
import com.JobApplicationPortal.Job_portal.job_service.Repository.JobApplicationRepository;
import com.JobApplicationPortal.Job_portal.job_service.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.job_service.Service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    @Autowired
    private RestTemplate restTemplate;
    private final String AUTH_SERVICE_URL = "http://auth-service";

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    // ---------------- Helper Methods ----------------
    private User getLoggedInEmployer(HttpSession session) {
        String email = (String) session.getAttribute("email");
        String token = (String) session.getAttribute("token");
        if (email == null || token == null) return null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        User user = restTemplate.exchange(
                AUTH_SERVICE_URL + "/auth/user?email=" + email,
                HttpMethod.GET,
                entity,
                User.class
        ).getBody();

        if (user != null && "EMPLOYER".equalsIgnoreCase(user.getRole())) {
            return user;
        }
        return null;
    }

    private boolean isEmployer(HttpSession session) {
        return getLoggedInEmployer(session) != null;
    }

    // ------------------ Thymeleaf Views ------------------

    @GetMapping("/dashboard")
    public String employerDashboard(HttpSession session) {
        if (!isEmployer(session)) return "redirect:/login";
        return "employer-dashboard";
    }

    @GetMapping("/create-job")
    public String createJobForm(HttpSession session) {
        if (!isEmployer(session)) return "redirect:/login";
        return "create-job";
    }

    @PostMapping("/create-job")
    public String createJobSubmit(@ModelAttribute Job job, HttpSession session) {
        User employer = getLoggedInEmployer(session);
        if (employer == null) return "redirect:/login";

        job.setEmployer(employer);
        jobRepository.save(job);
        return "redirect:/employer/my-jobs";
    }

    @GetMapping("/my-jobs")
    public String myJobs(HttpSession session, Model model) {
        User employer = getLoggedInEmployer(session);
        if (employer == null) return "redirect:/login";

        List<Job> jobs = jobRepository.findAll().stream()
                .filter(j -> j.getEmployer().getId().equals(employer.getId()))
                .toList();
        model.addAttribute("jobs", jobs);
        return "my-jobs";
    }

    @GetMapping("/job/{jobId}/applicants")
    public String viewApplicants(@PathVariable Long jobId, HttpSession session, Model model) {
        User employer = getLoggedInEmployer(session);
        if (employer == null) return "redirect:/login";

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        if (!job.getEmployer().getId().equals(employer.getId())) return "redirect:/employer/my-jobs";

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

        User employer = getLoggedInEmployer(session);
        if (employer == null) return "redirect:/login";

        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        if (!application.getJob().getEmployer().getId().equals(employer.getId())) {
            return "redirect:/employer/my-jobs";
        }

        application.setStatus(status);
        applicationRepository.save(application);

        return "redirect:/employer/job/" + application.getJob().getId() + "/applicants";
    }
}
