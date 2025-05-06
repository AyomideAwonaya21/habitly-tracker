package com.ayo.habitly.controller;

import com.ayo.habitly.dto.UserDTO;
import com.ayo.habitly.model.User;
import com.ayo.habitly.repository.UserRepository;
import com.ayo.habitly.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
        System.out.println("🔑 Login attempt with email: " + loginRequest.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isEmpty()) {
            System.out.println("❌ User not found for email: " + loginRequest.getEmail());
            return ResponseEntity.badRequest().body("Invalid email or password.");
        }

        User user = optionalUser.get();
        boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        System.out.println("🔎 Password match result: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("❌ Password does not match for email: " + loginRequest.getEmail());
            return ResponseEntity.badRequest().body("Invalid email or password.");
        }

        // ✅ Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // ✅ Prepare response with token + safe user info
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful.");
        response.put("token", token);

        // Map User -> UserDTO (no sensitive data)
        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getName());
        response.put("user", userDTO);

        System.out.println("✅ Login successful for email: " + loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();

        // Map each User to UserDTO
        List<UserDTO> userDTOs = users.stream()
                .map(user -> new UserDTO(user.getId(), user.getEmail(), user.getName()))
                .toList();

        return ResponseEntity.ok(userDTOs);
    }
}
