package com.revhire.userservice.controller;

import com.revhire.userservice.model.User;
import com.revhire.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats/users")
@RequiredArgsConstructor
public class UserStatisticsController {

    private final UserRepository userRepository;

    @GetMapping("/by-role")
    public ResponseEntity<Map<String, Long>> getUsersByRole() {
        Map<String, Long> stats = new HashMap<>();
        for (User user : userRepository.findAll()) {
            stats.put(user.getRole().name(), stats.getOrDefault(user.getRole().name(), 0L) + 1);
        }
        return ResponseEntity.ok(stats);
    }
}
