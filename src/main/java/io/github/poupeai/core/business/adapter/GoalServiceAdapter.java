package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.audit.Log;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.domain.port.business.GoalServicePort;
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
public class GoalServiceAdapter implements GoalServicePort {
    private final GoalRepositoryPort goalRepositoryPort;

    @Override
    @Transactional
    public Goal create(Goal goal) {
        Goal saved = goalRepositoryPort.create(goal);

        Log.event(log, "GOAL_CREATED", "Meta financeira criada. ID: {}, Nome: {}", saved.getId(), saved.getName());

        return saved;
    }

    @Override
    @Transactional
    public Goal update(Goal goal) {
        Goal updated = goalRepositoryPort.update(goal);

        Log.event(log, "GOAL_UPDATED", "Meta financeira atualizada. ID: {}", updated.getId());

        return updated;
    }

    @Override
    public Goal findByIdAndProfileId(UUID id, UUID profileId) {
        return goalRepositoryPort.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada."));
    }

    @Override
    public List<Goal> findAllByProfileId(UUID profileId) {
        return goalRepositoryPort.findAllByProfileId(profileId);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID profileId) {
        if (!goalRepositoryPort.existsByIdAndProfileId(id, profileId)) {
            throw new ResourceNotFoundException("Meta não encontrada.");
        }
        goalRepositoryPort.delete(id);

        Log.event(log, "GOAL_DELETED", "Meta financeira excluída. ID: {}", id);
    }
}
