package io.github.poupeai.core.audit;

import io.github.poupeai.core.persistence.entity.AuditLogEntity;
import io.github.poupeai.core.persistence.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileAuditEventHandlerTest {

    @InjectMocks
    private ProfileAuditEventHandler handler;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("Should save audit log when handling CREATE event")
    void handleProfileAuditEvent_shouldSaveAuditLog() {
        UUID profileId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        String sourceIp = "192.168.1.1";

        ProfileAuditEvent event = new ProfileAuditEvent(
                profileId,
                "CREATE",
                "Profile",
                null,
                sourceIp,
                correlationId);

        when(auditLogRepository.save(any(AuditLogEntity.class))).thenAnswer(i -> {
            AuditLogEntity entity = i.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        handler.handleProfileAuditEvent(event);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogEntity savedLog = captor.getValue();
        assertEquals("CREATE", savedLog.getActionType());
        assertEquals("Profile", savedLog.getEntityType());
        assertEquals(profileId, savedLog.getProfileId());
        assertEquals(profileId.toString(), savedLog.getEntityId());
        assertEquals(sourceIp, savedLog.getSourceIp());
        assertEquals(correlationId, savedLog.getCorrelationId());
        assertEquals("poupeai-core-service", savedLog.getServiceName());
    }

    @Test
    @DisplayName("Should save audit log with changes for UPDATE event")
    void handleProfileAuditEvent_shouldSaveChangesForUpdate() {
        UUID profileId = UUID.randomUUID();
        Map<String, Object> changes = Map.of(
                "firstName", new Object[] { "Old", "New" },
                "email", new Object[] { "old@test.com", "new@test.com" });

        ProfileAuditEvent event = new ProfileAuditEvent(
                profileId,
                "UPDATE",
                "Profile",
                changes,
                null,
                null);

        when(auditLogRepository.save(any(AuditLogEntity.class))).thenAnswer(i -> i.getArgument(0));

        handler.handleProfileAuditEvent(event);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogEntity savedLog = captor.getValue();
        assertEquals("UPDATE", savedLog.getActionType());
        assertNotNull(savedLog.getChanges());
        assertEquals(2, savedLog.getChanges().size());
    }

    @Test
    @DisplayName("Should not throw when repository fails")
    void handleProfileAuditEvent_shouldNotThrowOnRepositoryError() {
        ProfileAuditEvent event = new ProfileAuditEvent(
                UUID.randomUUID(),
                "CREATE",
                "Profile",
                null,
                null,
                null);

        when(auditLogRepository.save(any(AuditLogEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> handler.handleProfileAuditEvent(event));
    }
}
