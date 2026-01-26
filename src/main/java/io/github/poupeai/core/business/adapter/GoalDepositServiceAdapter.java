package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.domain.port.business.GoalDepositServicePort;
import io.github.poupeai.core.domain.port.persistence.GoalDepositRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.GoalRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalDepositServiceAdapter implements GoalDepositServicePort {
    private final GoalDepositRepositoryPort goalDepositRepositoryPort;
    private final GoalRepositoryPort goalRepositoryPort;

    @Override
    @Transactional
    public GoalDeposit create(GoalDeposit goalDeposit, UUID profileId) {
        if (!goalRepositoryPort.existsByIdAndProfileId(goalDeposit.getGoalId(), profileId)) {
            throw new ResourceNotFoundException("Meta não encontrada.");
        }
        return goalDepositRepositoryPort.create(goalDeposit);
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
    }
}
