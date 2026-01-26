package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.GoalDeposit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalDepositRepositoryPort {
    GoalDeposit create(GoalDeposit goalDeposit);
    List<GoalDeposit> findAllByGoalId(UUID goalId);
    Optional<GoalDeposit> findByIdAndGoalId(UUID id, UUID goalId);
    void delete(UUID id);
}
