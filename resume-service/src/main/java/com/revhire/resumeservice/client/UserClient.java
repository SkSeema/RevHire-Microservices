package com.revhire.resumeservice.client;

import com.revhire.resumeservice.dto.external.JobSeekerProfileDto;
import com.revhire.resumeservice.dto.external.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/users/internal/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/seeker/profile/user/{userId}")
    JobSeekerProfileDto getProfileByUserId(@PathVariable("userId") Long userId);
}
