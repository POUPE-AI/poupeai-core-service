package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.persistence.entity.GoalDepositEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalDepositEntityMapper {
    @Mapping(target = "goalId", source = "goal.id")
    GoalDeposit toDomain(GoalDepositEntity entity);

    @Mapping(target = "goal", ignore = true)
    GoalDepositEntity toEntity(GoalDeposit domain);

    List<GoalDeposit> toDomainList(List<GoalDepositEntity> entities);

    List<GoalDepositEntity> toEntityList(List<GoalDeposit> domains);
}
