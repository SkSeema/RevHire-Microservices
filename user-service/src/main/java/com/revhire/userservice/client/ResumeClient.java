package com.revhire.userservice.client;

import com.revhire.userservice.dto.external.ResumeDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "resume-service")
public interface ResumeClient {

    @GetMapping("/api/resumes/internal/profile/{seekerId}/details")
    ResumeDetailsDto getResumeDetails(@PathVariable("seekerId") Long seekerId);
}
