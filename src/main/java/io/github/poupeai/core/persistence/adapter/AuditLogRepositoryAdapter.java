package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.AuditLog;
import io.github.poupeai.core.domain.port.persistence.AuditLogRepositoryPort;
import io.github.poupeai.core.persistence.mapper.AuditLogEntityMapper;
import io.github.poupeai.core.persistence.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {
    private final AuditLogRepository repository;
    private final AuditLogEntityMapper mapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        try {
            var entity = mapper.toEntity(auditLog);
            var savedEntity = repository.save(entity);
            return mapper.toDomain(savedEntity);
        } catch (Exception e) {
            log.error("Falha ao salvar log de auditoria para entidade {} com id {}: {}",
                    auditLog.getEntityType(), auditLog.getEntityId(), e.getMessage(), e);
            return null;
        }
    }
}
