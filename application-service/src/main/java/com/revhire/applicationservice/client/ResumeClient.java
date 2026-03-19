package com.revhire.applicationservice.client;

import com.revhire.applicationservice.dto.external.ResumeDetailsDto;
import com.revhire.applicationservice.dto.external.ResumeFileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "resume-service")
public interface ResumeClient {
    @GetMapping("/api/resumes/{id}")
    ResumeFileDto getResumeFileById(@PathVariable("id") Long id);
    
    @GetMapping("/api/resumes/seeker/{seekerId}")
    ResumeFileDto getResumeByJobSeekerId(@PathVariable("seekerId") Long seekerId);

    @GetMapping("/api/resumes/seeker/{seekerId}/download")
    org.springframework.http.ResponseEntity<byte[]> downloadResumeBySeeker(@PathVariable("seekerId") Long seekerId);

    @GetMapping("/api/resumes/{id}/download")
    org.springframework.http.ResponseEntity<byte[]> downloadResumeById(@PathVariable("id") Long id);

    @GetMapping("/api/resumes/internal/profile/{seekerId}/details")
    ResumeDetailsDto getResumeDetails(@PathVariable("seekerId") Long seekerId);
}
