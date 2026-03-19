package com.revhire.notificationservice.service;

import com.revhire.notificationservice.model.Notification;

import com.revhire.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void createNotification(Long userId, String userEmail, String message) {
        createNotification(userId, userEmail, message, false, null, null);
    }

    @Transactional
    public void createNotification(Long userId, String userEmail, String message, boolean sendEmail, String subject, String emailBody) {
        log.info("Creating notification for user {}: {}", userEmail, message);
        if (userId != null) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle("Update");
            notification.setMessage(message);
            notification.setType("INFO");
            notification.setRead(false);
            notificationRepository.save(notification);
        } else {
            log.info("Skipping notification persistence because no userId is available yet");
        }

        log.info("Email dispatch requested: {}, userEmail={}", sendEmail, userEmail);
        if (sendEmail && emailService != null && userEmail != null) {
            try {
                if (subject != null && emailBody != null) {
                    emailService.sendEmail(userEmail, subject, emailBody);
                } else {
                    emailService.sendEmail(userEmail, "RevHire Notification", message);
                }
            } catch (Exception e) {
                log.error("Failed to send email notification to {}", userEmail, e);
            }
        }
    }

    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Notification getNotificationById(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return notification;
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(all);
    }
}
