package io.github.poupeai.core.web.mapper.goal;

import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.web.dto.goal.GoalRequest;
import io.github.poupeai.core.web.dto.goal.GoalResponse;
import io.github.poupeai.core.web.dto.goal.GoalUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface GoalControllerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Goal toDomain(GoalRequest request, UUID profileId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDomainFromDto(GoalUpdateRequest dto, @MappingTarget Goal domain);

    GoalResponse toResponse(Goal domain);
    
    List<GoalResponse> toResponseList(List<Goal> goals);
}
