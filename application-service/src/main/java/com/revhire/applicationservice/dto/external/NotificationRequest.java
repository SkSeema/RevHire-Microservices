package com.revhire.applicationservice.dto.external;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long userId;
    private String userEmail;
    private String message;
    private boolean sendEmail;
    private String subject;
    private String emailBody;
}
