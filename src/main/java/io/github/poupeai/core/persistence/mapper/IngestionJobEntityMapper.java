package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.persistence.entity.IngestionJobEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngestionJobEntityMapper {
    IngestionJob toDomain(IngestionJobEntity entity);

    IngestionJobEntity toEntity(IngestionJob domain);
}
