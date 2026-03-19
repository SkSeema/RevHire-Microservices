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
@RequestMapping("/api/stats/applications")
@RequiredArgsConstructor
public class ApplicationStatisticsController {

    private final ApplicationRepository applicationRepository;

    @GetMapping("/by-status")
    public ResponseEntity<Map<String, Long>> getApplicationsByStatus() {
        Map<String, Long> stats = new HashMap<>();
        applicationRepository.findAll()
                .forEach(a -> stats.put(a.getStatus().name(), stats.getOrDefault(a.getStatus().name(), 0L) + 1));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/conversion-rate")
    public ResponseEntity<Double> getConversionRate() {
        long total = applicationRepository.count();
        if (total == 0) {
            return ResponseEntity.ok(0.0);
        }
        long selected = applicationRepository.countByStatus(Application.ApplicationStatus.SELECTED);
        return ResponseEntity.ok((double) selected / total);
    }
}
