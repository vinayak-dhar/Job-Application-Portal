package com.jobportal.auth.Service;

import com.jobportal.auth.Entity.User;
import com.jobportal.auth.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Signup: store password as plain text (for testing)
    public User signup(User user) {
        return userRepository.save(user);
    }

    // Login: plain text password comparison
    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElse(null);
    }
}
