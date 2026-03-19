package com.revhire.applicationservice.dto.external;

import lombok.Data;
import java.time.LocalDate;

@Data
public class JobPostDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String salary;
    private String jobType;
    private LocalDate deadline;
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private Long createdById;
    private String status;
}
