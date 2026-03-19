package com.revhire.userservice.controller;

import com.revhire.userservice.dto.response.UserResponse;
import com.revhire.userservice.model.User;
import com.revhire.userservice.repository.UserRepository; // Added import for UserRepository
import com.revhire.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository; // Added UserRepository dependency

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<UserResponse> getInternalUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.updateUserStatus(id, enabled));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestParam User.Role role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        return ResponseEntity.ok(userService.getUserCount());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<User.Role>> getRoles() {
        return ResponseEntity.ok(Arrays.asList(User.Role.values()));
    }

    @GetMapping("/internal/metrics")
    public ResponseEntity<Map<String, Long>> getInternalMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("totalUsers", userRepository.count());
        metrics.put("seekers", userRepository.countByRole(User.Role.JOB_SEEKER));
        metrics.put("employers", userRepository.countByRole(User.Role.EMPLOYER));
        metrics.put("activeUsers", userRepository.countByStatus(true));
        metrics.put("inactiveUsers", userRepository.countByStatus(false));
        return ResponseEntity.ok(metrics);
    }
}
