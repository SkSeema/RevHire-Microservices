package com.revhire.applicationservice.controller;

import com.revhire.applicationservice.dto.response.ApplicationStatusHistoryResponse;
import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.model.ApplicationStatusHistory;
import com.revhire.applicationservice.repository.ApplicationRepository;
import com.revhire.applicationservice.repository.ApplicationStatusHistoryRepository;
import com.revhire.applicationservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/history")
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatusHistoryController {

    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationRepository applicationRepository;

    @GetMapping
    public ResponseEntity<?> getHistoryForApplication(@PathVariable("applicationId") Long applicationId,
                                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Application not found");
        }

        // Simple security check: any authenticated user can view history for now
        // In a real app, you'd check if the user is the applicant or the job owner
        
        List<ApplicationStatusHistoryResponse> history = historyRepository
                .findByApplicationIdOrderByChangedAtDesc(applicationId).stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(history);
    }

    private ApplicationStatusHistoryResponse mapToDto(ApplicationStatusHistory history) {
        ApplicationStatusHistoryResponse dto = new ApplicationStatusHistoryResponse();
        dto.setId(history.getId());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedByUserName("User ID: " + history.getChangedById());
        dto.setComment(history.getComment());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }
}
