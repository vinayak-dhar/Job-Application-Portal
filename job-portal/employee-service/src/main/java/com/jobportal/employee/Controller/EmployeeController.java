package com.jobportal.employee.Controller;

import com.jobportal.employee.Entity.Job;
import com.jobportal.employee.Entity.JobApplication;
import com.jobportal.employee.Entity.User;
import com.jobportal.employee.Repository.JobRepository;
import com.jobportal.employee.Repository.JobApplicationRepository;
import com.jobportal.employee.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String AUTH_SIGNUP_URL = "redirect:http://localhost:8081/signup";

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long userId, HttpSession httpSession) {
        if (userId != null) {
            httpSession.setAttribute("userId", userId); // store userId in session
        }

        if (httpSession.getAttribute("userId") == null) {
            return "redirect:/login"; // redirect to login if userId not found
        }

        return "employee-dashboard";
    }


    // View Jobs
    @GetMapping("/jobs")
    public String viewJobs(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("userId") == null) return AUTH_SIGNUP_URL;

        model.addAttribute("jobs", jobRepository.findAll());
        return "employee-jobs";
    }

    // Apply for Job
    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId, HttpSession session) {
        // Check session for userId
        if (session.getAttribute("userId") == null) return "redirect:/login"; // or AUTH_SIGNUP_URL

        // Fetch employee using userId
        Long employeeId = (Long) session.getAttribute("userId");
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // Fetch job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        // Check if already applied
        if (applicationRepository.findByEmployeeAndJob(employee, job).isPresent()) {
            return "redirect:/employee/jobs?error=alreadyApplied";
        }

        // Save application
        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setEmployee(employee);
        application.setStatus("PENDING");
        applicationRepository.save(application);

        return "redirect:/employee/applications";
    }



    // View My Applications
    @GetMapping("/applications")
    public String viewApplications(HttpSession httpSession, Model model) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) return AUTH_SIGNUP_URL;

        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        List<JobApplication> applications = applicationRepository.findByEmployee(employee);
        model.addAttribute("applications", applications);
        return "employee-applications";
    }

    // Withdraw Application
    @PostMapping("/applications/{applicationId}/withdraw")
    public String withdrawApplication(@PathVariable Long applicationId,
                                      HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return AUTH_SIGNUP_URL;

        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));

        if (!application.getEmployee().getId().equals(employee.getId())) {
            return "redirect:/employee/applications?error=notAllowed";
        }

        application.setStatus("WITHDRAWN");
        applicationRepository.save(application);

        return "redirect:/employee/applications?success=withdrawn";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return AUTH_SIGNUP_URL;
    }
}
