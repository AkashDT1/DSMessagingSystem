package com.logiq.backend.controller;

import com.logiq.backend.model.User;
import com.logiq.backend.repository.UserRepository;
import com.logiq.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // The BCrypt tool we made in SecurityConfig

    @PostMapping("/register")
    public String register(@RequestBody User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()) != null) {
            return "FAILURE: Email is already registered!";
        }

        if (newUser.getRole() == null) {
            newUser.setRole("CUSTOMER");
        }

        // SCRAMBLE THE PASSWORD BEFORE SAVING
        String hashedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        userRepository.save(newUser);
        return "SUCCESS: Account created! You can now log in.";
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {
        User dbUser = userRepository.findByEmail(loginRequest.getEmail());

        // CHECK IF PASSWORD MATCHES THE HASH
        if (dbUser != null && passwordEncoder.matches(loginRequest.getPassword(), dbUser.getPassword())) {

            // GENERATE THE JWT TOKEN
            String token = JwtUtil.generateToken(dbUser.getEmail(), dbUser.getRole());

            return "SUCCESS: " + token;
        } else {
            return "FAILURE: Invalid Credentials";
        }
    }
}