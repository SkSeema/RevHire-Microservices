package com.revhire.jobservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "user-service", contextId = "jobUserMetricsClient")
public interface UserClient {

    @GetMapping("/api/users/internal/metrics")
    Map<String, Long> getMetrics();
}
