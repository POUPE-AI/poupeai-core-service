package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.GoalDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalDepositRepository extends JpaRepository<GoalDepositEntity, UUID> {
    List<GoalDepositEntity> findAllByGoal_Id(UUID goalId);
    Optional<GoalDepositEntity> findByIdAndGoal_Id(UUID id, UUID goalId);
}
