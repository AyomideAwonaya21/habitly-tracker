package com.ayo.habitly.controller;

import com.ayo.habitly.dto.HabitDTO; // ✅ Add this import
import com.ayo.habitly.model.Habit;
import com.ayo.habitly.model.User;
import com.ayo.habitly.repository.HabitRepository;
import com.ayo.habitly.repository.UserRepository;
import com.ayo.habitly.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public HabitController(HabitRepository habitRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createHabit(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody Habit habitRequest) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtUtil.extractClaims(token);
        String email = claims.getSubject();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = userOptional.get();
        habitRequest.setUser(user);
        Habit savedHabit = habitRepository.save(habitRequest);

        // ✅ Convert to HabitDTO before returning
        HabitDTO habitDTO = new HabitDTO(
            savedHabit.getId(),
            savedHabit.getTitle(),
            savedHabit.getDescription(),
            savedHabit.isCompleted(),
            user.getId(),
            user.getEmail(),
            user.getName()
        );

        return ResponseEntity.ok(habitDTO);
    }

    @GetMapping
    public ResponseEntity<?> getHabits(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtUtil.extractClaims(token);
        String email = claims.getSubject();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = userOptional.get();
        List<Habit> habits = habitRepository.findByUserId(user.getId());

        // ✅ Convert each Habit to HabitDTO
        List<HabitDTO> habitDTOs = habits.stream()
                .map(habit -> new HabitDTO(
                        habit.getId(),
                        habit.getTitle(),
                        habit.getDescription(),
                        habit.isCompleted(),
                        user.getId(),
                        user.getEmail(),
                        user.getName()
                ))
                .toList();

        return ResponseEntity.ok(habitDTOs);
    }
}
