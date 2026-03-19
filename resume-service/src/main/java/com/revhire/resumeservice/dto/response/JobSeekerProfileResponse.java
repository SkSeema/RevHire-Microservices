package com.revhire.resumeservice.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class JobSeekerProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String headline;
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
    private String appliedRole;
    private Long jobId;
    private List<String> skillsList;
}
