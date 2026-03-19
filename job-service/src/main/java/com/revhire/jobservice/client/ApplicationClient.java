package com.revhire.jobservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "application-service")
public interface ApplicationClient {

    @GetMapping("/api/applications/internal/metrics")
    Map<String, Long> getMetrics();

    @GetMapping("/api/applications/job/{jobId}")
    List<Object> getApplicationsByJob(@PathVariable("jobId") Long jobId);
}
