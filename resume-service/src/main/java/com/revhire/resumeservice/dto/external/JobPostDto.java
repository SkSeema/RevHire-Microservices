package com.revhire.resumeservice.dto.external;

import lombok.Data;

@Data
public class JobPostDto {
    private Long id;
    private String title;
    private Long companyId;
    private String companyName;
}
