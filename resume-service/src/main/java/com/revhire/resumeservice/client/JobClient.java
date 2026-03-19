package com.revhire.resumeservice.client;

import com.revhire.resumeservice.dto.external.JobPostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service")
public interface JobClient {
    @GetMapping("/api/jobs/{id}")
    JobPostDto getJobById(@PathVariable("id") Long id);
}
