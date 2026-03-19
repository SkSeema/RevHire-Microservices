package com.revhire.resumeservice.controller;

import com.revhire.resumeservice.client.UserClient;
import com.revhire.resumeservice.dto.external.JobSeekerProfileDto;
import com.revhire.resumeservice.model.ResumeFiles;
import com.revhire.resumeservice.security.UserPrincipal;
import com.revhire.resumeservice.service.JobSeekerResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LegacySeekerResumeController {

    private final JobSeekerResumeService resumeService;
    private final UserClient userClient;

    @PostMapping("/api/seeker/profile/resume")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        try {
            ResumeFiles savedFile = resumeService.storeFile(file, resolveSeekerId(user.getId()));
            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/seeker/profile/{seekerId}/resume/download")
    public ResponseEntity<?> downloadResume(@PathVariable("seekerId") Long seekerId) {
        try {
            Resource resource = resumeService.loadResumeResource(seekerId);
            ResumeFiles resumeFile = resumeService.getResumeFile(seekerId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resumeFile.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Long resolveSeekerId(Long userId) {
        JobSeekerProfileDto profile = userClient.getProfileByUserId(userId);
        if (profile == null || profile.getId() == null) {
            throw new RuntimeException("Job seeker profile not found");
        }
        return profile.getId();
    }
}
