package com.JobApplicationPortal.Job_portal.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.JobApplicationPortal.Job_portal.Entity.User;
import com.JobApplicationPortal.Job_portal.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Signup
    public User signup(User user) {
        return userRepository.save(user);
    }

    // Login
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user; // login success
        }
        return null; // login failed
    }
}

