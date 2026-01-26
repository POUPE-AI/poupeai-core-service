package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    List<GoalEntity> findAllByProfile_UserId(UUID profileId);
    Optional<GoalEntity> findByIdAndProfile_UserId(UUID id, UUID profileId);
    boolean existsByIdAndProfile_UserId(UUID id, UUID profileId);
}
