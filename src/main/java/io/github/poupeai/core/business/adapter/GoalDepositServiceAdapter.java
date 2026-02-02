package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.audit.Log;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.domain.port.business.GoalDepositServicePort;
import io.github.poupeai.core.domain.port.persistence.GoalDepositRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.GoalRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalDepositServiceAdapter implements GoalDepositServicePort {
    private final GoalDepositRepositoryPort goalDepositRepositoryPort;
    private final GoalRepositoryPort goalRepositoryPort;

    @Override
    @Transactional
    public GoalDeposit create(GoalDeposit goalDeposit, UUID profileId) {
        if (!goalRepositoryPort.existsByIdAndProfileId(goalDeposit.getGoalId(), profileId)) {
            throw new ResourceNotFoundException("Meta não encontrada.");
        }
        GoalDeposit saved = goalDepositRepositoryPort.create(goalDeposit);

        Log.event(log, "GOAL_DEPOSIT_CREATED", "Depósito em meta realizado. ID: {}, Valor: {}", saved.getId(), saved.getDepositAmount());

        return saved;
    }

    @Override
    public List<GoalDeposit> findAllByGoalId(UUID goalId, UUID profileId) {
        if (!goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)) {
            throw new ResourceNotFoundException("Meta não encontrada.");
        }
        return goalDepositRepositoryPort.findAllByGoalId(goalId);
    }

    @Override
    @Transactional
    public void delete(UUID goalId, UUID depositId, UUID profileId) {
        if (!goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)) {
            throw new ResourceNotFoundException("Meta não encontrada.");
        }

        var deposit = goalDepositRepositoryPort.findByIdAndGoalId(depositId, goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Depósito não encontrado."));

        goalDepositRepositoryPort.delete(depositId);

        Log.event(log, "GOAL_DEPOSIT_DELETED", "Depósito em meta excluído. ID: {}", depositId);
    }
}
