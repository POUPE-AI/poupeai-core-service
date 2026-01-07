package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.persistence.entity.GoalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    Goal toDomain(GoalEntity entity);

    @Mapping(target = "profile", ignore = true)
    GoalEntity toEntity(Goal domain);

    List<Goal> toDomainList(List<GoalEntity> entities);

    List<GoalEntity> toEntityList(List<Goal> domains);
}
