package com.jobportal.auth.Controller;

import com.jobportal.auth.Entity.User;
import com.jobportal.auth.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@ModelAttribute User user, Model model) {
        try {
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
    public String loginSubmit(@ModelAttribute User user, Model model) {
        User loggedIn = userService.login(user.getEmail(), user.getPassword());
        if (loggedIn != null) {
            loggedIn.setRole(loggedIn.getRole().toUpperCase());
            System.out.println(loggedIn.getEmail());
            System.out.println(loggedIn.getPassword());
            System.out.println(loggedIn.getRole());

            // Redirect with session ID as query param (for testing)
            switch (loggedIn.getRole()) {
                case "ADMIN":
                    return "redirect:http://localhost:8082/admin/dashboard?userId=" + loggedIn.getId();
                case "EMPLOYER":
                    return "redirect:http://localhost:8083/employer/dashboard?userId=" + loggedIn.getId();
                case "EMPLOYEE":
                    return "redirect:http://localhost:8084/employee/dashboard?userId=" + loggedIn.getId();
            }
        }
        model.addAttribute("error", "Invalid email or password!");
        return "login";
    }
}
