package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.AuditLog;
import io.github.poupeai.core.persistence.entity.AuditLogEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogEntityMapper {
    AuditLogEntity toEntity(AuditLog domain);

    AuditLog toDomain(AuditLogEntity entity);
}
