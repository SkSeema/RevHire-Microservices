package com.revhire.userservice.client;

import com.revhire.userservice.dto.external.AuditLogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", contextId = "userAuditLogClient")
public interface AuditLogClient {
    @PostMapping("/api/audit-logs/internal/create")
    void createAuditLog(@RequestBody AuditLogRequest request);
}
