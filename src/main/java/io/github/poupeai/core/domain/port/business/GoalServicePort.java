package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.Goal;

import java.util.List;
import java.util.UUID;

public interface GoalServicePort {
    Goal create(Goal goal);
    Goal update(Goal goal);
    Goal findByIdAndProfileId(UUID id, UUID profileId);
    List<Goal> findAllByProfileId(UUID profileId);
    void delete(UUID id, UUID profileId);
}
