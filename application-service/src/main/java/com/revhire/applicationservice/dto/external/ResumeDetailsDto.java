package com.revhire.applicationservice.dto.external;

import lombok.Data;

import java.util.List;

@Data
public class ResumeDetailsDto {
    private String title;
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
    private List<Object> skillsList;
    private boolean resumeUploaded;
    private boolean profileSummarySet;
    private boolean skillsSet;
    private boolean experienceSet;
    private boolean educationSet;
    private int completionPercentage;
}
