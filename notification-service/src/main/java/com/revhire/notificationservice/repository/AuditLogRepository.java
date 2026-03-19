package com.revhire.notificationservice.repository;

import com.revhire.notificationservice.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByChangedByIdOrderByChangedAtDesc(Long userId);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);

    List<AuditLog> findByEntityType(String entityType);

    void deleteByChangedById(Long userId);
}
