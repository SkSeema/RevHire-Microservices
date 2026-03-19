package com.revhire.applicationservice.dto.external;

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
