package com.ayo.habitly.controller;

import com.ayo.habitly.model.User;
import com.ayo.habitly.repository.UserRepository;
import com.ayo.habitly.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered.");
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid email or password.");
        }

        User user = optionalUser.get();
        boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());

        if (!passwordMatches) {
            return ResponseEntity.badRequest().body("Invalid email or password.");
        }

        // ✅ Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // ✅ Prepare response with token + user details
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful.");
        response.put("token", token);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
