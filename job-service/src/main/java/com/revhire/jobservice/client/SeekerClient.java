package com.revhire.jobservice.client;

import com.revhire.jobservice.dto.external.JobSeekerProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", contextId = "jobSeekerSearchClient")
public interface SeekerClient {

    @GetMapping("/api/seeker/profile/search")
    List<JobSeekerProfileDto> searchSeekers(@RequestParam("keyword") String keyword);
}
