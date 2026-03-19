package com.revhire.applicationservice.client;

import com.revhire.applicationservice.dto.external.AuditLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", contextId = "applicationAuditLogClient")
public interface AuditLogClient {
    @PostMapping("/api/audit/internal/log")
    void logAction(@RequestBody AuditLogRequest request);
}
