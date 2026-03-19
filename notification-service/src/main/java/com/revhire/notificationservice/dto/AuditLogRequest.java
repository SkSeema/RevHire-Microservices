package com.revhire.notificationservice.dto;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String entityType;
    private Long entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private Long changedById;
}
