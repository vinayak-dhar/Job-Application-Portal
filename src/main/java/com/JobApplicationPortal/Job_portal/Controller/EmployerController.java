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
}
