package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.AuditLog;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
}
