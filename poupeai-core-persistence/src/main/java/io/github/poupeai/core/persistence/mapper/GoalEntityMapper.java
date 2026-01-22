package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.persistence.entity.GoalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "colorHex", ignore = true)
    @Mapping(target = "initialBalance", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    Goal toDomain(GoalEntity entity);

    @Mapping(target = "profile", ignore = true)
    GoalEntity toEntity(Goal domain);

    List<Goal> toDomainList(List<GoalEntity> entities);
}
