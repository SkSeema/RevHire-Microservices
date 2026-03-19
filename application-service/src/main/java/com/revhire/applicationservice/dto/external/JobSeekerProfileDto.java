package com.revhire.applicationservice.dto.external;

import lombok.Data;

@Data
public class JobSeekerProfileDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String headline;
    private String summary;
    private String location;
    private String profileImage;
    private UserDto user;
}
