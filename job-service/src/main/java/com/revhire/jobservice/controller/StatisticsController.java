package com.revhire.jobservice.controller;

import com.revhire.jobservice.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatisticsController {

    private final JobPostRepository jobPostRepository;

    @GetMapping({"/api/job-stats/by-location", "/api/stats/jobs/by-location"})
    public ResponseEntity<Map<String, Long>> getJobsByLocation() {
        Map<String, Long> stats = new HashMap<>();
        jobPostRepository.findAll()
                .forEach(j -> stats.put(j.getLocation(), stats.getOrDefault(j.getLocation(), 0L) + 1));
        return ResponseEntity.ok(stats);
    }

    @GetMapping({"/api/job-stats/by-type", "/api/stats/jobs/by-type"})
    public ResponseEntity<Map<String, Long>> getJobsByType() {
        Map<String, Long> stats = new HashMap<>();
        jobPostRepository.findAll()
                .forEach(j -> stats.put(j.getJobType(), stats.getOrDefault(j.getJobType(), 0L) + 1));
        return ResponseEntity.ok(stats);
    }

    @GetMapping({"/api/job-stats/trending", "/api/stats/jobs/trending"})
    public ResponseEntity<List<String>> getTrendingJobs() {
        // This could be enhanced to actually calculate trending jobs from real data
        return ResponseEntity.ok(List.of("Software Engineer", "Data Scientist", "Java Developer", "React Developer"));
    }
}
