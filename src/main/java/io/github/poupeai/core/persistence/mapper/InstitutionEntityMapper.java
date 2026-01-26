package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.persistence.entity.InstitutionEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstitutionEntityMapper {
    Institution toDomain(InstitutionEntity entity);

    InstitutionEntity toEntity(Institution domain);

    List<Institution> toDomainList(List<InstitutionEntity> entities);
}
