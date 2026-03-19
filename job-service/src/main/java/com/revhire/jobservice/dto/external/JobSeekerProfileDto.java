package com.revhire.jobservice.dto.external;

import lombok.Data;

import java.util.List;

@Data
public class JobSeekerProfileDto {
    private Long id;
    private String name;
    private String email;
    private String headline;
    private String location;
    private String skills;
    private List<?> skillsList;
}
