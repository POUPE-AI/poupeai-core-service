package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.domain.port.persistence.GoalRepositoryPort;
import io.github.poupeai.core.persistence.mapper.GoalEntityMapper;
import io.github.poupeai.core.persistence.repository.GoalDepositRepository;
import io.github.poupeai.core.persistence.repository.GoalRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GoalRepositoryAdapter implements GoalRepositoryPort {
    private final GoalRepository goalRepository;
    private final GoalEntityMapper goalMapper;
    private final ProfileRepository profileRepository;
    private final GoalDepositRepository goalDepositRepository;

    @Override
    public Goal create(Goal goal) {
        var entity = goalMapper.toEntity(goal);
        
        var profile = profileRepository.getReferenceById(goal.getProfileId());
        entity.setProfile(profile);
        
        var savedEntity = goalRepository.save(entity);
        Goal domain = goalMapper.toDomain(savedEntity);
        domain.setInitialBalance(domain.getInitialBalance() == null ? BigDecimal.ZERO : domain.getInitialBalance());
        domain.setCurrentBalance(calculateCurrentBalance(domain.getId(), domain.getInitialBalance()));
        return domain;
    }

    @Override
    public Goal update(Goal goal) {
        var existingGoal = goalRepository.findById(goal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada."));
        
        var profile = profileRepository.getReferenceById(goal.getProfileId());
        existingGoal.setProfile(profile);
        existingGoal.setName(goal.getName());
        existingGoal.setDescription(goal.getDescription());
        existingGoal.setColorHex(goal.getColorHex());
        existingGoal.setInitialBalance(goal.getInitialBalance());
        existingGoal.setGoalAmount(goal.getGoalAmount());
        existingGoal.setTargetDate(goal.getTargetDate());
        existingGoal.setCompletedAt(goal.getCompletedAt());

        var savedEntity = goalRepository.save(existingGoal);
        Goal domain = goalMapper.toDomain(savedEntity);
        domain.setInitialBalance(domain.getInitialBalance() == null ? BigDecimal.ZERO : domain.getInitialBalance());
        domain.setCurrentBalance(calculateCurrentBalance(domain.getId(), domain.getInitialBalance()));
        return domain;
    }

    @Override
    public Optional<Goal> findByIdAndProfileId(UUID id, UUID profileId) {
        return goalRepository.findByIdAndProfile_UserId(id, profileId).map(entity -> {
            Goal domain = goalMapper.toDomain(entity);
            domain.setInitialBalance(domain.getInitialBalance() == null ? BigDecimal.ZERO : domain.getInitialBalance());
            domain.setCurrentBalance(calculateCurrentBalance(domain.getId(), domain.getInitialBalance()));
            return domain;
        });
    }

    @Override
    public List<Goal> findAllByProfileId(UUID profileId) {
        var entities = goalRepository.findAllByProfile_UserId(profileId);
        var domains = goalMapper.toDomainList(entities);
        for (Goal domain : domains) {
            domain.setInitialBalance(domain.getInitialBalance() == null ? BigDecimal.ZERO : domain.getInitialBalance());
            domain.setCurrentBalance(calculateCurrentBalance(domain.getId(), domain.getInitialBalance()));
        }
        return domains;
    }

    @Override
    public void delete(UUID id) {
        goalRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndProfileId(UUID id, UUID profileId) {
        return goalRepository.existsByIdAndProfile_UserId(id, profileId);
    }

    private BigDecimal calculateCurrentBalance(UUID goalId, BigDecimal initialBalance) {
        var deposits = goalDepositRepository.findAllByGoal_Id(goalId);
        BigDecimal sum = deposits.stream()
            .map(e -> e.getDepositAmount() == null ? BigDecimal.ZERO : e.getDepositAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return (initialBalance == null ? BigDecimal.ZERO : initialBalance).add(sum);
    }
}
