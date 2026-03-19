package com.revhire.applicationservice.dto.external;

import lombok.Data;

@Data
public class ResumeFileDto {
    private Long id;
    private Long jobSeekerId;
    private String fileName;
    private String filePath;
}
