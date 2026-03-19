package com.revhire.userservice.dto.external;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String entityName;
    private Long entityId;
    private String action;
    private String fieldName;
    private String changeDescription;
    private Long changedById;
}
