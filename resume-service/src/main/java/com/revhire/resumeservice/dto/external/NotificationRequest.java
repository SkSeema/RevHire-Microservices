package com.revhire.resumeservice.dto.external;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long userId;
    private String message;
}
