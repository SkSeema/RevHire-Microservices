package com.revhire.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "application-service")
public interface ApplicationClient {

    @GetMapping("/api/applications/my-applications")
    List<Object> getMyApplications(); // Using Object for simplicity as we just need to pass it through

    @GetMapping("/api/applications/user/{userId}")
    List<Object> getApplicationsByUserId(@PathVariable("userId") Long userId);
}
