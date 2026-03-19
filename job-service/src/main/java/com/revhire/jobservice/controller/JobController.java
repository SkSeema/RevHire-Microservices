package com.revhire.jobservice.controller;

import com.revhire.jobservice.client.ApplicationClient;
import com.revhire.jobservice.dto.request.JobPostRequest;
import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.dto.response.SkillResponse;
import com.revhire.jobservice.model.JobPost;
import com.revhire.jobservice.security.UserPrincipal;
import com.revhire.jobservice.service.JobService;
import com.revhire.jobservice.service.JobSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;
    private final JobSearchService searchService;
    private final ApplicationClient applicationClient;

    @PostMapping
    public ResponseEntity<?> createJob(@Valid @RequestBody JobPostRequest jobDto,
                                       @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can post jobs");
        }
        try {
            JobPostResponse job = jobService.createJob(jobDto, user);
            return ResponseEntity.ok(job);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<JobPostResponse>> getAllJobs(
            @RequestParam(required = false, name = "title") String title,
            @RequestParam(required = false, name = "location") String location,
            @RequestParam(required = false, name = "experience") Integer experience,
            @RequestParam(required = false, name = "company") String company,
            @RequestParam(required = false, name = "salary") Double salary,
            @RequestParam(required = false, name = "jobType") List<String> jobType,
            @RequestParam(required = false, name = "daysAgo") Integer daysAgo) {

        return ResponseEntity
                .ok(searchService.searchJobs(title, location, experience, company, salary, jobType, daysAgo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(jobService.mapToDto(jobService.getJobById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<?> getMyJobs(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        return ResponseEntity.ok(jobService.getMyJobs(user));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_JOB_SEEKER"))) {
            return ResponseEntity.status(403).body("Unauthorized: Only Job Seekers can get recommendations");
        }
        return ResponseEntity.ok(jobService.getRecommendedJobs(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable("id") Long id, @Valid @RequestBody JobPostRequest jobDto,
                                       @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can edit jobs");
        }
        try {
            JobPostResponse updatedJob = jobService.updateJob(id, jobDto, user);
            return ResponseEntity.ok(updatedJob);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateJobStatus(@PathVariable("id") Long id,
                                             @RequestParam("status") JobPost.JobStatus status,
                                             @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can update job status");
        }
        try {
            JobPostResponse updatedJob = jobService.updateJobStatus(id, status, user);
            return ResponseEntity.ok(updatedJob);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable("id") Long id,
                                       @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can delete jobs");
        }
        try {
            jobService.deleteJob(id, user); 
            return ResponseEntity.ok("Job deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<?> getJobSkills(@PathVariable("id") Long id) {
        try {
            List<SkillResponse> skills = jobService.getJobSkills(id);
            return ResponseEntity.ok(skills);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<?> getJobApplications(@PathVariable("id") Long id) {
        return ResponseEntity.ok(applicationClient.getApplicationsByJob(id));
    }
}
