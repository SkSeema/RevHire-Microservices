package com.revhire.jobservice.controller;

import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.security.UserPrincipal;
import com.revhire.jobservice.service.SavedJobsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seeker/saved-jobs")
@RequiredArgsConstructor
@Slf4j
public class SavedJobsController {

    private final SavedJobsService savedJobsService;

    @PostMapping("/{jobId}")
    public ResponseEntity<?> saveJob(@PathVariable("jobId") Long jobId,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            savedJobsService.saveJob(jobId, principal.getId());
            return ResponseEntity.ok("Job saved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> unsaveJob(@PathVariable("jobId") Long jobId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            savedJobsService.unsaveJob(jobId, principal.getId());
            return ResponseEntity.ok("Job unsaved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getSavedJobs(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<JobPostResponse> savedJobs = savedJobsService.getSavedJobs(principal.getId());
        return ResponseEntity.ok(savedJobs);
    }

    @GetMapping("/{jobId}/check")
    public ResponseEntity<?> isJobSaved(@PathVariable("jobId") Long jobId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        // Simplified check, could be moved to service
        boolean isSaved = savedJobsService.getSavedJobs(principal.getId()).stream()
                .anyMatch(j -> j.getId().equals(jobId));
        return ResponseEntity.ok(isSaved);
    }
}
