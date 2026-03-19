package com.revhire.resumeservice.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SeekerResumeDetailsResponse {
    private String title;
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
    private List<SkillSummaryResponse> skillsList;
    private boolean resumeUploaded;
    private boolean profileSummarySet;
    private boolean skillsSet;
    private boolean experienceSet;
    private boolean educationSet;
    private int completionPercentage;
}
