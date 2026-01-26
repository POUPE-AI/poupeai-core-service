package io.github.poupeai.core.web.mapper.goal;

import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.web.dto.goal.GoalDepositRequest;
import io.github.poupeai.core.web.dto.goal.GoalDepositResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface GoalDepositControllerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goalId", source = "goalId")
    @Mapping(target = "createdAt", ignore = true)
    GoalDeposit toDomain(GoalDepositRequest request, UUID goalId);

    GoalDepositResponse toResponse(GoalDeposit domain);
    
    List<GoalDepositResponse> toResponseList(List<GoalDeposit> deposits);
}
