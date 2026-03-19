package com.revhire.resumeservice.client;

import com.revhire.resumeservice.dto.external.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/api/notifications/internal/create")
    void createNotification(@RequestBody NotificationRequest request);
}
