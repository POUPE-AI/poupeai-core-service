package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.persistence.entity.ProfileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileEntityMapper {
    Profile toDomain(ProfileEntity entity);
    ProfileEntity toEntity(Profile domain);
}
