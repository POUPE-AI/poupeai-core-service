package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.domain.port.persistence.GoalDepositRepositoryPort;
import io.github.poupeai.core.persistence.mapper.GoalDepositEntityMapper;
import io.github.poupeai.core.persistence.repository.GoalDepositRepository;
import io.github.poupeai.core.persistence.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GoalDepositRepositoryAdapter implements GoalDepositRepositoryPort {
    private final GoalDepositRepository goalDepositRepository;
    private final GoalDepositEntityMapper goalDepositMapper;
    private final GoalRepository goalRepository;

    @Override
    public GoalDeposit create(GoalDeposit goalDeposit) {
        var entity = goalDepositMapper.toEntity(goalDeposit);
        
        var goal = goalRepository.getReferenceById(goalDeposit.getGoalId());
        entity.setGoal(goal);
        
        var savedEntity = goalDepositRepository.save(entity);
        return goalDepositMapper.toDomain(savedEntity);
    }

    @Override
    public List<GoalDeposit> findAllByGoalId(UUID goalId) {
        var entities = goalDepositRepository.findAllByGoal_Id(goalId);
        return goalDepositMapper.toDomainList(entities);
    }

    @Override
    public Optional<GoalDeposit> findByIdAndGoalId(UUID id, UUID goalId) {
        return goalDepositRepository.findByIdAndGoal_Id(id, goalId).map(goalDepositMapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        goalDepositRepository.deleteById(id);
    }
}
