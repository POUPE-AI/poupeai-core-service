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
import java.math.RoundingMode;

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

    @Mapping(target = "description", source = "description")
    @Mapping(target = "colorHex", source = "colorHex")
    @Mapping(target = "initialBalance", source = "initialBalance")
    @Mapping(target = "currentBalance", source = "currentBalance")
    @Mapping(target = "percentageCompleted", expression = "java(calculatePercentage(domain))")
    GoalResponse toResponse(Goal domain);
    
    List<GoalResponse> toResponseList(List<Goal> goals);

    default java.math.BigDecimal calculatePercentage(Goal domain) {
        if (domain == null) {
            return null;
        }
        java.math.BigDecimal goal = domain.getGoalAmount();
        java.math.BigDecimal current = domain.getCurrentBalance();
        if (goal == null || goal.compareTo(java.math.BigDecimal.ZERO) == 0 || current == null) {
            return java.math.BigDecimal.ZERO;
        }
        return current.divide(goal, 4, RoundingMode.HALF_UP).multiply(new java.math.BigDecimal("100"));
    }
}
