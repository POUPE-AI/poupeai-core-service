package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.persistence.entity.GoalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "colorHex", source = "colorHex")
    @Mapping(target = "initialBalance", source = "initialBalance")
    @Mapping(target = "currentBalance", ignore = true)
    Goal toDomain(GoalEntity entity);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "description", source = "description")
    @Mapping(target = "colorHex", source = "colorHex")
    @Mapping(target = "initialBalance", source = "initialBalance")
    GoalEntity toEntity(Goal domain);

    List<Goal> toDomainList(List<GoalEntity> entities);
}
