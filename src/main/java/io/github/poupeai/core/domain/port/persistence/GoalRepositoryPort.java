package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.Goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepositoryPort {
    Goal create(Goal goal);
    Goal update(Goal goal);
    Optional<Goal> findByIdAndProfileId(UUID id, UUID profileId);
    List<Goal> findAllByProfileId(UUID profileId);
    void delete(UUID id);
    boolean existsByIdAndProfileId(UUID id, UUID profileId);
}
