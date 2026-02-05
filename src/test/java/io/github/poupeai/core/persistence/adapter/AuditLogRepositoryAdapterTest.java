package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.AuditLog;
import io.github.poupeai.core.persistence.entity.AuditLogEntity;
import io.github.poupeai.core.persistence.mapper.AuditLogEntityMapper;
import io.github.poupeai.core.persistence.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryAdapterTest {

    @InjectMocks
    private AuditLogRepositoryAdapter adapter;

    @Mock
    private AuditLogRepository repository;

    @Mock
    private AuditLogEntityMapper mapper;

    @Test
    @DisplayName("Should save audit log and return domain object")
    void save_shouldPersistAndReturnDomain() {
        UUID profileId = UUID.randomUUID();
        AuditLog domainLog = createAuditLog(profileId);
        AuditLogEntity entity = createAuditLogEntity(profileId);
        AuditLogEntity savedEntity = createAuditLogEntity(profileId);
        savedEntity.setId(1L);
        AuditLog savedDomain = createAuditLog(profileId);
        savedDomain.setId(1L);

        when(mapper.toEntity(domainLog)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        AuditLog result = adapter.save(domainLog);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(mapper).toEntity(domainLog);
        verify(repository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("Should return null and log error when exception occurs")
    void save_shouldReturnNullOnException() {
        UUID profileId = UUID.randomUUID();
        AuditLog domainLog = createAuditLog(profileId);
        AuditLogEntity entity = createAuditLogEntity(profileId);

        when(mapper.toEntity(domainLog)).thenReturn(entity);
        when(repository.save(entity)).thenThrow(new RuntimeException("Database error"));

        AuditLog result = adapter.save(domainLog);

        assertNull(result);
        verify(mapper).toEntity(domainLog);
        verify(repository).save(entity);
        verify(mapper, never()).toDomain(any());
    }

    private AuditLog createAuditLog(UUID profileId) {
        return AuditLog.builder()
                .profileId(profileId)
                .actionTime(OffsetDateTime.now())
                .actionType("CREATE")
                .entityType("Profile")
                .entityId(profileId.toString())
                .changes(Map.of())
                .serviceName("poupeai-core-service")
                .build();
    }

    private AuditLogEntity createAuditLogEntity(UUID profileId) {
        return AuditLogEntity.builder()
                .profileId(profileId)
                .actionTime(OffsetDateTime.now())
                .actionType("CREATE")
                .entityType("Profile")
                .entityId(profileId.toString())
                .changes(Map.of())
                .serviceName("poupeai-core-service")
                .build();
    }
}
