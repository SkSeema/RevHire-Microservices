package com.revhire.notificationservice.service;

import com.revhire.notificationservice.model.AuditLog;
import com.revhire.notificationservice.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void logActionSavesMappedAuditLog() {
        auditLogService.logAction("JOB", 12L, "UPDATE", "old", "new", 7L);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("JOB", captor.getValue().getEntityType());
        assertEquals(12L, captor.getValue().getEntityId());
        assertEquals("UPDATE", captor.getValue().getAction());
        assertEquals("old", captor.getValue().getOldValue());
        assertEquals("new", captor.getValue().getNewValue());
        assertEquals(7L, captor.getValue().getChangedById());
    }

    @Test
    void logActionSwallowsRepositoryFailures() {
        doThrow(new RuntimeException("db down")).when(auditLogRepository).save(org.mockito.ArgumentMatchers.any(AuditLog.class));

        assertDoesNotThrow(() ->
                auditLogService.logAction("JOB", 12L, "UPDATE", "old", "new", 7L)
        );
    }
}
