package com.revhire.applicationservice.controller;

import com.revhire.applicationservice.model.Application;
import com.revhire.applicationservice.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/applications/internal")
@RequiredArgsConstructor
public class ApplicationInternalController {

    private final ApplicationRepository applicationRepository;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Long>> getMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("totalApplications", applicationRepository.count());
        metrics.put("applied", applicationRepository.countByStatus(Application.ApplicationStatus.APPLIED));
        metrics.put("shortlisted", applicationRepository.countByStatus(Application.ApplicationStatus.SHORTLISTED));
        metrics.put("selected", applicationRepository.countByStatus(Application.ApplicationStatus.SELECTED));
        metrics.put("rejected", applicationRepository.countByStatus(Application.ApplicationStatus.REJECTED));
        return ResponseEntity.ok(metrics);
    }
}
