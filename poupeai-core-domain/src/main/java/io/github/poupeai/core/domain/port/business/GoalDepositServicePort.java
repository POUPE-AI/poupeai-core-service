package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.GoalDeposit;

import java.util.List;
import java.util.UUID;

public interface GoalDepositServicePort {
    GoalDeposit create(GoalDeposit goalDeposit, UUID profileId);
    List<GoalDeposit> findAllByGoalId(UUID goalId, UUID profileId);
    void delete(UUID goalId, UUID depositId, UUID profileId);
}
