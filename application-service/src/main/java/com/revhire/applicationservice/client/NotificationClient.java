package com.revhire.applicationservice.client;

import com.revhire.applicationservice.dto.external.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", contextId = "applicationNotificationClient")
public interface NotificationClient {
    @PostMapping("/api/notifications/internal/create")
    void createNotification(@RequestBody NotificationRequest request);
}
