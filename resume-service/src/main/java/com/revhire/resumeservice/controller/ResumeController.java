package com.revhire.resumeservice.controller;

import com.revhire.resumeservice.model.ResumeFiles;
import com.revhire.resumeservice.security.UserPrincipal;
import com.revhire.resumeservice.service.JobSeekerResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final JobSeekerResumeService resumeService;

    @PostMapping({"/upload", "/api/seeker/profile/resume"})
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        try {
            ResumeFiles savedFile = resumeService.storeFile(file, user.getId());
            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/seeker/{seekerId}")
    public ResponseEntity<?> getResumeBySeeker(@PathVariable("seekerId") Long seekerId) {
        ResumeFiles resume = resumeService.getResumeFile(seekerId);
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resume);
    }

    @GetMapping({"/api/seeker/profile/{seekerId}/resume/download"})
    public ResponseEntity<?> downloadResumeLegacy(@PathVariable("seekerId") Long seekerId) {
        return downloadResumeBySeeker(seekerId);
    }

    @GetMapping("/seeker/{seekerId}/download")
    public ResponseEntity<?> downloadResumeBySeeker(@PathVariable("seekerId") Long seekerId) {
        try {
            ResumeFiles resumeFile = resumeService.getResumeFile(seekerId);
            if (resumeFile == null) {
                return ResponseEntity.notFound().build();
            }
            return buildDownloadResponse(resumeFile);
        } catch (Exception e) {
            log.error("Error downloading resume", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadResumeById(@PathVariable("id") Long id) {
        return resumeService.getResumeFileById(id)
                .map(this::buildDownloadResponse)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResumeById(@PathVariable("id") Long id) {
        // Implementation for getting by ID if needed by application-service
        return resumeService.getResumeFileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/internal/profile/{seekerId}/details")
    public ResponseEntity<?> getResumeDetails(@PathVariable("seekerId") Long seekerId) {
        return ResponseEntity.ok(resumeService.getResumeDetails(seekerId));
    }

    private ResponseEntity<?> buildDownloadResponse(ResumeFiles resumeFile) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/resumes")
                    .resolve(resumeFile.getFilePath())
                    .normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(
                    filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            if (resumeFile.getFileName().endsWith(".pdf")) contentType = "application/pdf";
            else if (resumeFile.getFileName().endsWith(".doc")) contentType = "application/msword";
            else if (resumeFile.getFileName().endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resumeFile.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error building resume response", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
