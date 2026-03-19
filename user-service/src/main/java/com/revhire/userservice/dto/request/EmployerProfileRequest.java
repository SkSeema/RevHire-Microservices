package com.revhire.userservice.dto.request;

import lombok.Data;

@Data
public class EmployerProfileRequest {
    private String jobTitle;
    private String department;
    private Long companyId;
}
