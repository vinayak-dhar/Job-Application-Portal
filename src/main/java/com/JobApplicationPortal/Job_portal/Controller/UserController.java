package com.JobApplicationPortal.Job_portal.Controller;

import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Signup
    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        return userService.signup(user);
    }

    // Login
    @PostMapping("/login")
    public String login(@RequestBody User user, HttpSession session) {
        User loggedIn = userService.login(user.getEmail(), user.getPassword());
        if (loggedIn != null) {
            session.setAttribute("user", loggedIn); // store in session
            return "Login Successful! Welcome " + loggedIn.getRole();
        }
        return "Invalid email or password!";
    }

    // Check who is logged in
    @GetMapping("/whoami")
    public String whoAmI(HttpSession session) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null) {
            return "No user is logged in!";
        }
        return "You are logged in as: " + loggedIn.getRole() + " (" + loggedIn.getEmail() + ")";
    }

    // Logout
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "You have been logged out!";
    }
}
