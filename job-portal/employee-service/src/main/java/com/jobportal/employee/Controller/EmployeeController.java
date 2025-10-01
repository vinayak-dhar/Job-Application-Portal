package com.jobportal.employee.Controller;

import com.jobportal.employee.Entity.Job;
import com.jobportal.employee.Entity.JobApplication;
import com.jobportal.employee.Repository.JobRepository;
import com.jobportal.employee.Repository.JobApplicationRepository;
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

    private static final String AUTH_LOGIN_URL = "redirect:http://localhost:8081/login";

    // ------------------ Thymeleaf Views ------------------

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String sessionId,
                            HttpSession session) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;
        return "employee-dashboard";
    }

    @GetMapping("/jobs")
    public String viewJobs(@RequestParam(required = false) String sessionId,
                           HttpSession session,
                           Model model) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        List<Job> jobs = jobRepository.findAll();
        model.addAttribute("jobs", jobs);
        return "employee-jobs";
    }

    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId,
                              @RequestParam(required = false) String sessionId,
                              HttpSession session) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        // Use sessionId directly as employee identifier
        Long employeeId = (Long) session.getAttribute("employeeId");

        if (applicationRepository.findByEmployeeIdAndJobId(employeeId, jobId).isPresent()) {
            return "redirect:/employee/jobs?error=alreadyApplied";
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setEmployeeId(employeeId);
        application.setStatus("PENDING");

        applicationRepository.save(application);
        return "redirect:/employee/applications";
    }

    @GetMapping("/applications")
    public String viewMyApplications(@RequestParam(required = false) String sessionId,
                                     HttpSession session,
                                     Model model) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        Long employeeId = (Long) session.getAttribute("employeeId");
        List<JobApplication> applications = applicationRepository.findByEmployeeId(employeeId);
        model.addAttribute("applications", applications);
        return "employee-applications";
    }

    @PostMapping("/applications/{applicationId}/withdraw")
    public String withdrawApplication(@PathVariable Long applicationId,
                                      @RequestParam(required = false) String sessionId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (sessionId != null) session.setAttribute("sessionId", sessionId);
        if (session.getAttribute("sessionId") == null) return AUTH_LOGIN_URL;

        Long employeeId = (Long) session.getAttribute("employeeId");

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        if (!application.getEmployeeId().equals(employeeId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot withdraw someone else's application.");
            return "redirect:/employee/applications";
        }

        if ("WITHDRAWN".equals(application.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This application is already withdrawn.");
            return "redirect:/employee/applications";
        }

        application.setStatus("WITHDRAWN");
        applicationRepository.save(application);

        redirectAttributes.addFlashAttribute("success", "Application withdrawn successfully!");
        return "redirect:/employee/applications";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return AUTH_LOGIN_URL;
    }
}
