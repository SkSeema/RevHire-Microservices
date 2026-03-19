package com.revhire.notificationservice.controller;

import com.revhire.notificationservice.model.Notification;
import com.revhire.notificationservice.security.UserPrincipal;
import com.revhire.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getMyNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<Notification> notifications = notificationService.getMyNotifications(userPrincipal.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<Notification> notifications = notificationService.getUnreadNotifications(userPrincipal.getId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            notificationService.markAsRead(id, userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        notificationService.markAllAsRead(userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable("id") Long id,
                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            notificationService.deleteNotification(id, userPrincipal.getId());
            return ResponseEntity.ok("Notification deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        notificationService.deleteAllNotifications(userPrincipal.getId());
        return ResponseEntity.ok("All notifications deleted");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable("id") Long id,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            return ResponseEntity.ok(notificationService.getNotificationById(id, userPrincipal.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/internal/create")
    public ResponseEntity<?> createNotification(@RequestBody com.revhire.notificationservice.dto.NotificationRequest request) {
        log.info("Internal notification request for user ID: {}", request.getUserId());
        notificationService.createNotification(
                request.getUserId(),
                request.getUserEmail(),
                request.getMessage(),
                request.isSendEmail(),
                request.getSubject(),
                request.getEmailBody()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(@RequestBody Notification notification) {
        return ResponseEntity.ok("Broadcast sent");
    }
}
