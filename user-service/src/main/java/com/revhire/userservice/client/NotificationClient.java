package com.revhire.userservice.client;

import com.revhire.userservice.dto.external.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", contextId = "userNotificationClient", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/internal/create")
    void createNotification(@RequestBody NotificationRequest request);
}
