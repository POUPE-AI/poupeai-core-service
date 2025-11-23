package io.github.poupeai.persistence.mapper;

import io.github.poupeai.domain.model.Profile;
import io.github.poupeai.persistence.entity.ProfileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileEntityMapper {
    Profile toDomain(ProfileEntity entity);
    ProfileEntity toEntity(Profile domain);
}
