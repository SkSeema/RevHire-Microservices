package com.revhire.applicationservice.controller;

import com.revhire.applicationservice.dto.response.ApplicationResponse;
import com.revhire.applicationservice.dto.response.ApplicationSummaryResponse;
import com.revhire.applicationservice.dto.request.ApplicationRequest;
import com.revhire.applicationservice.dto.request.BulkApplicationStatusRequest;
import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.security.UserPrincipal;
import com.revhire.applicationservice.service.ApplicationService;
import com.revhire.applicationservice.service.ApplicationWithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationWithdrawalService withdrawalService;

    @PostMapping("/apply/{jobId}")
    public ResponseEntity<?> applyForJob(@PathVariable("jobId") Long jobId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationResponse application = applicationService.applyForJob(jobId, user, null, null);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/apply-v2")
    public ResponseEntity<?> applyForJobV2(@RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationResponse application = applicationService.applyForJob(
                    request.getJobId(), user, request.getResumeFileId(), request.getCoverLetter());
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/check/{jobId}")
    public ResponseEntity<Boolean> hasApplied(@PathVariable("jobId") Long jobId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"))) {
            return ResponseEntity.ok(false);
        }
        try {
            List<ApplicationResponse> apps = applicationService.getMyApplications(user);
            boolean applied = apps.stream().anyMatch(a -> a.getJobId().equals(jobId));
            return ResponseEntity.ok(applied);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.getMyApplications(user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all-for-employer")
    public ResponseEntity<?> getApplicationsForEmployer(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsForEmployer(user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicationsForJob(@PathVariable("jobId") Long jobId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsForJob(jobId, user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("applicationId") Long applicationId,
            @RequestParam("status") Application.ApplicationStatus status,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationResponse application = applicationService.updateApplicationStatus(applicationId, status, user);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{applicationId}/withdraw")
    public ResponseEntity<?> withdrawApplication(@PathVariable("applicationId") Long applicationId,
            @RequestParam(required = false, name = "reason") String reason,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationResponse application = withdrawalService.withdrawApplication(applicationId, reason, user);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/bulk-status")
    public ResponseEntity<?> updateBulkStatus(@RequestBody BulkApplicationStatusRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.updateBulkStatus(
                    request.getApplicationIds(), request.getStatus(), user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<?> deleteApplication(@PathVariable("applicationId") Long applicationId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            applicationService.deleteApplication(applicationId, user);
            return ResponseEntity.ok("Application deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/job/{jobId}/search")
    public ResponseEntity<?> searchApplicantsForJob(@PathVariable("jobId") Long jobId,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "skill") String skill,
            @RequestParam(required = false, name = "experience") String experience,
            @RequestParam(required = false, name = "education") String education,
            @RequestParam(required = false, name = "appliedAfter") String appliedAfter,
            @RequestParam(required = false, name = "status") Application.ApplicationStatus status,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.searchApplicantsForJob(
                    jobId, name, skill, experience, education, appliedAfter, status, user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/job/{jobId}/summary")
    public ResponseEntity<?> getApplicationSummary(@PathVariable("jobId") Long jobId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationSummaryResponse summary = applicationService.getApplicationSummary(jobId, user);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{applicationId}/resume/download")
    public ResponseEntity<?> downloadResume(@PathVariable("applicationId") Long applicationId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can download resumes.");
        }
        try {
            org.springframework.http.ResponseEntity<byte[]> response = applicationService.downloadResume(applicationId, user);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error downloading resume", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(applicationService.getApplicationsByUserId(userId));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getApplicationById(@PathVariable("applicationId") Long applicationId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        return applicationService.getApplicationById(applicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
