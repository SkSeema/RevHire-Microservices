package com.revhire.applicationservice.client;

import com.revhire.applicationservice.dto.external.JobSeekerProfileDto;
import com.revhire.applicationservice.dto.external.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/users/internal/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/seeker/profile/user/{userId}")
    JobSeekerProfileDto getProfileByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/api/seeker/profile/id/{profileId}")
    JobSeekerProfileDto getProfileById(@PathVariable("profileId") Long profileId);
}
