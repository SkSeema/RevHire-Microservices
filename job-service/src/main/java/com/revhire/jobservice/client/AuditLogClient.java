package com.revhire.jobservice.client;

import com.revhire.jobservice.dto.external.AuditLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface AuditLogClient {
    @PostMapping("/api/audit/log")
    void logAction(@RequestBody AuditLogRequest request);
}
