package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.domain.port.business.GoalServicePort;
import io.github.poupeai.core.domain.port.persistence.GoalRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalServiceAdapter implements GoalServicePort {
    private final GoalRepositoryPort goalRepositoryPort;

    @Override
    @Transactional
    public Goal create(Goal goal) {
        return goalRepositoryPort.create(goal);
    }

    @Override
    @Transactional
    public Goal update(Goal goal) {
        return goalRepositoryPort.update(goal);
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
    }
}
