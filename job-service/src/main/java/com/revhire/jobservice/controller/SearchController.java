package com.revhire.jobservice.controller;

import com.revhire.jobservice.client.SeekerClient;
import com.revhire.jobservice.dto.external.JobSeekerProfileDto;
import com.revhire.jobservice.dto.response.JobPostResponse;
import com.revhire.jobservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SeekerClient seekerClient;

    @GetMapping("/jobs")
    public ResponseEntity<List<JobPostResponse>> searchJobs(@RequestParam String keyword) {
        return ResponseEntity.ok(searchService.searchJobs(keyword));
    }

    @GetMapping("/seekers")
    public ResponseEntity<List<JobSeekerProfileDto>> searchSeekers(@RequestParam String keyword) {
        return ResponseEntity.ok(seekerClient.searchSeekers(keyword));
    }
}
