package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Entity.Job;
import com.JobApplicationPortal.Job_portal.Entity.JobApplication;
import com.JobApplicationPortal.Job_portal.Repository.UserRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobRepository;
import com.JobApplicationPortal.Job_portal.Repository.JobApplicationRepository;
import com.JobApplicationPortal.Job_portal.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {


    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;



    // Landing page
    @GetMapping("/")
    public String landingPage() {
        return "index";
    }

    // Show signup page
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // Handle signup
    @PostMapping("/signup")
    public String signupSubmit(@ModelAttribute User user, Model model) {
        try {
            userService.signup(user);
            return "redirect:/login"; // ✅ redirect to login page
        } catch (Exception e) {
            model.addAttribute("error", "Signup failed: " + e.getMessage());
            return "signup";
        }
    }

    // Show login page
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    // Handle login
    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute User user, HttpSession session, Model model) {
        User loggedIn = userService.login(user.getEmail(), user.getPassword());
        if (loggedIn != null) {
            session.setAttribute("user", loggedIn);

            switch (loggedIn.getRole()) {
                case "admin": return "redirect:/admin/dashboard";
                case "employer": return "redirect:/employer/dashboard";
                case "employee": return "redirect:/employee/dashboard";
            }
        }
        model.addAttribute("error", "Invalid email or password!");
        return "login";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // -------------------- Admin Pages --------------------

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"admin".equalsIgnoreCase(loggedIn.getRole())) {
            return "redirect:/login";
        }
        return "admin-dashboard";
    }

    @GetMapping("/admin/manage-users")
    public String manageUsers(@RequestParam(required = false) String role, Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"admin".equalsIgnoreCase(loggedIn.getRole())) return "redirect:/login";

        List<User> users = (role != null)
                ? userRepository.findAll().stream().filter(u -> u.getRole().equalsIgnoreCase(role)).toList()
                : userRepository.findAll();

        model.addAttribute("users", users);
        return "manage-users";
    }

    @PostMapping("/admin/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"admin".equalsIgnoreCase(loggedIn.getRole())) return "redirect:/login";

        userRepository.deleteById(id);
        return "redirect:/admin/manage-users";
    }

    @GetMapping("/admin/monitor-jobs")
    public String monitorJobs(Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"admin".equalsIgnoreCase(loggedIn.getRole())) return "redirect:/login";

        model.addAttribute("jobs", jobRepository.findAll());
        return "monitor-jobs";
    }

    @GetMapping("/admin/monitor-applications")
    public String monitorApplications(Model model, HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"admin".equalsIgnoreCase(loggedIn.getRole())) return "redirect:/login";

        model.addAttribute("applications", applicationRepository.findAll());
        return "monitor-applications";
    }


    // ----------------------
    // EMPLOYER DASHBOARD
    // ----------------------
    @GetMapping("/employer/dashboard")
    public String employerDashboard(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }
        return "employer-dashboard"; // employer-dashboard.html
    }

    @GetMapping("/employer/create-job")
    public String createJobForm(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }
        return "create-job"; // create-job.html
    }

    @GetMapping("/employer/my-jobs")
    public String myJobs(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }
        List<Job> jobs = jobRepository.findAll().stream()
                .filter(j -> j.getEmployer().getId().equals(loggedIn.getId()))
                .toList();
        model.addAttribute("jobs", jobs);
        return "my-jobs"; // my-jobs.html
    }

    @GetMapping("/employer/job/{jobId}/applicants")
    public String viewApplicants(@PathVariable Long jobId, HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"EMPLOYER".equals(loggedIn.getRole())) {
            return "redirect:/login";
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));
        if (!job.getEmployer().getId().equals(loggedIn.getId())) {
            return "redirect:/employer/my-jobs";
        }
        List<JobApplication> applicants = applicationRepository.findByJob(job);
        model.addAttribute("job", job);
        model.addAttribute("applicants", applicants);
        return "applicants"; // applicants.html
    }
}
