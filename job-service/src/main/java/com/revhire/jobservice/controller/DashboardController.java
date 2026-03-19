package com.revhire.jobservice.controller;

import com.revhire.jobservice.client.ApplicationClient;
import com.revhire.jobservice.client.UserClient;
import com.revhire.jobservice.model.JobPost;
import com.revhire.jobservice.repository.JobPostRepository;
import com.revhire.jobservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final JobPostRepository jobPostRepository;
    private final UserClient userClient;
    private final ApplicationClient applicationClient;

    @GetMapping("/admin/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminMetrics() {
        try {
            log.info("Fetching job service admin metrics...");
            Map<String, Object> metrics = new HashMap<>();

            try {
                metrics.putAll(userClient.getMetrics());
            } catch (Exception e) {
                log.warn("User metrics unavailable", e);
            }

            metrics.put("totalJobs", jobPostRepository.count());
            metrics.put("activeJobs", jobPostRepository.countByStatus(JobPost.JobStatus.ACTIVE));
            metrics.put("closedJobs", jobPostRepository.countByStatus(JobPost.JobStatus.CLOSED));

            try {
                metrics.putAll(applicationClient.getMetrics());
            } catch (Exception e) {
                log.warn("Application metrics unavailable", e);
            }

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error fetching admin metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/employer/metrics")
    public ResponseEntity<Map<String, Object>> getEmployerMetrics(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> metrics = new HashMap<>();
        long totalJobs = jobPostRepository.findByCreatedById(principal.getId()).size();
        metrics.put("totalJobs", totalJobs);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/seeker/metrics")
    public ResponseEntity<Map<String, Object>> getSeekerMetrics(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("userId", principal.getId());
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/system/health")
    public ResponseEntity<Map<String, String>> getSystemHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("database", "CONNECTED");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/system/logs")
    public ResponseEntity<java.util.List<String>> getSystemLogs() {
        return ResponseEntity.ok(java.util.List.of("Job Service started", "Database connected"));
    }
}
