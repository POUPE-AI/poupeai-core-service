package io.github.poupeai.core.web.mapper.profile;

import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.web.dto.profile.ProfileRequest;
import io.github.poupeai.core.web.dto.profile.ProfileResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileControllerMapper {
    Profile toDomain(ProfileRequest dto);
    ProfileResponse toResponse(Profile domain);
}
