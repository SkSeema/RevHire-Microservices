package com.revhire.notificationservice.controller;

import com.revhire.notificationservice.dto.AuditLogRequest;
import com.revhire.notificationservice.dto.response.AuditLogResponse;
import com.revhire.notificationservice.model.AuditLog;
import com.revhire.notificationservice.repository.AuditLogRepository;
import com.revhire.notificationservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/public-test")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("Public test successful");
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll().stream().map(this::mapToDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getLogById(@PathVariable Long id) {
        return auditLogRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditLogRepository.findByEntityType(entityType).stream().map(this::mapToDto).toList());
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByEntity(@PathVariable String entityType,
                                                                  @PathVariable Long entityId) {
        return ResponseEntity.ok(
                auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId).stream()
                        .map(this::mapToDto)
                        .toList());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                auditLogRepository.findByChangedByIdOrderByChangedAtDesc(userId).stream().map(this::mapToDto).toList());
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupLogs(@RequestParam(required = false) Integer daysAgo) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/create")
    public ResponseEntity<?> createAuditLog(@RequestBody AuditLogRequest request) {
        return logAction(request);
    }

    @PostMapping("/internal/log")
    public ResponseEntity<?> logAction(@RequestBody AuditLogRequest request) {
        log.info("Internal audit log request for entity: {} ID: {}", request.getEntityType(), request.getEntityId());
        auditLogService.logAction(
                request.getEntityType(),
                request.getEntityId(),
                request.getAction(),
                request.getOldValue(),
                request.getNewValue(),
                request.getChangedById()
        );
        return ResponseEntity.ok().build();
    }

    private AuditLogResponse mapToDto(AuditLog logEntry) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setId(logEntry.getId());
        dto.setEntityType(logEntry.getEntityType());
        dto.setEntityId(logEntry.getEntityId());
        dto.setAction(logEntry.getAction());
        dto.setOldValue(logEntry.getOldValue());
        dto.setNewValue(logEntry.getNewValue());
        dto.setChangedAt(logEntry.getChangedAt());
        dto.setChangedByName(logEntry.getChangedById() != null ? "User #" + logEntry.getChangedById() : "System");
        dto.setDetails(logEntry.getAction() + " on " + logEntry.getEntityType() + " #" + logEntry.getEntityId());
        return dto;
    }
}
