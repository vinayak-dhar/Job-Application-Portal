package com.JobApplicationPortal.Job_portal.job_service.Controller;

import com.JobApplicationPortal.Job_portal.job_service.Entity.User;
//import com.JobApplicationPortal.Job_portal.job_service.Service.UserService;
import com.JobApplicationPortal.Job_portal.job_service.dto.JwtResponse;
import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
public class HomeController {

    @Autowired
    private RestTemplate restTemplate;
    private final String AUTH_SERVICE_URL = "http://auth-service"; // lowercase matches spring.application.name

    // ---------------- Landing / Signup / Login ----------------
    @GetMapping("/")
    public String landingPage() {
        return "index";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@ModelAttribute User user, Model model) {
        try {
            // Auth-service returns a simple string, so map response as String
            String response = restTemplate.postForObject(AUTH_SERVICE_URL + "/auth/signup", user, String.class);
            model.addAttribute("message", response);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Signup failed: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute User user, HttpSession session, Model model) {
        try {
            // Call auth-service for login
            JwtResponse jwtResponse = restTemplate.postForObject(
                    AUTH_SERVICE_URL + "/auth/login", user, JwtResponse.class);

            if (jwtResponse == null || jwtResponse.getToken() == null) {
                model.addAttribute("error", "Login failed: Invalid response from auth service");
                return "login";
            }

            // Save token + role in session
            session.setAttribute("token", jwtResponse.getToken());
            session.setAttribute("role", jwtResponse.getRole());
            session.setAttribute("email", jwtResponse.getEmail());

            switch (jwtResponse.getRole().toUpperCase()) {
                case "ADMIN": return "redirect:/admin/dashboard";
                case "EMPLOYER": return "redirect:/employer/dashboard";
                case "EMPLOYEE": return "redirect:/employee/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Invalid email or password!");
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}