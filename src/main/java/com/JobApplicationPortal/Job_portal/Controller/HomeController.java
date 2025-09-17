package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.User;
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
            // Normalize role to uppercase
            user.setRole(user.getRole().toUpperCase());
            userService.signup(user);
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
        User loggedIn = userService.login(user.getEmail(), user.getPassword());
        if (loggedIn != null) {
            // Normalize role to uppercase
            loggedIn.setRole(loggedIn.getRole().toUpperCase());
            session.setAttribute("user", loggedIn);

            switch (loggedIn.getRole()) {
                case "ADMIN":
                    return "redirect:/admin/dashboard";
                case "EMPLOYER":
                    return "redirect:/employer/dashboard";
                case "EMPLOYEE":
                    return "redirect:/employee/dashboard";
            }
        }
        model.addAttribute("error", "Invalid email or password!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}