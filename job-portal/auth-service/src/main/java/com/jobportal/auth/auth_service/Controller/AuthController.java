package com.jobportal.auth.auth_service.Controller;

import com.jobportal.auth.auth_service.Entity.User;
import com.jobportal.auth.auth_service.Service.AuthService;
import com.jobportal.auth.auth_service.dto.JwtResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        authService.signup(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        String token = authService.login(user.getEmail(), user.getPassword());
        User u = authService.getUserByEmail(user.getEmail());

        if (token == null || u == null) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        // Fetch user to get the role
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken(token);
        jwtResponse.setEmail(u.getEmail());
        jwtResponse.setRole(u.getRole());

        return ResponseEntity.ok(jwtResponse);
    }
}
