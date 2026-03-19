package com.revhire.jobservice.dto.external;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String entityName;
    private Long entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private Long changedById;
}
