package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.domain.port.persistence.GoalRepositoryPort;
import io.github.poupeai.core.persistence.mapper.GoalEntityMapper;
import io.github.poupeai.core.persistence.repository.GoalRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

    @Override
    public Goal create(Goal goal) {
        var entity = goalMapper.toEntity(goal);
        
        var profile = profileRepository.getReferenceById(goal.getProfileId());
        entity.setProfile(profile);
        
        var savedEntity = goalRepository.save(entity);
        return goalMapper.toDomain(savedEntity);
    }

    @Override
    public Goal update(Goal goal) {
        var existingGoal = goalRepository.findById(goal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada."));
        
        var profile = profileRepository.getReferenceById(goal.getProfileId());
        existingGoal.setProfile(profile);
        existingGoal.setName(goal.getName());
        existingGoal.setGoalAmount(goal.getGoalAmount());
        existingGoal.setTargetDate(goal.getTargetDate());
        existingGoal.setCompletedAt(goal.getCompletedAt());

        var savedEntity = goalRepository.save(existingGoal);
        return goalMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Goal> findByIdAndProfileId(UUID id, UUID profileId) {
        return goalRepository.findByIdAndProfile_UserId(id, profileId).map(goalMapper::toDomain);
    }

    @Override
    public List<Goal> findAllByProfileId(UUID profileId) {
        var entities = goalRepository.findAllByProfile_UserId(profileId);
        return goalMapper.toDomainList(entities);
    }

    @Override
    public void delete(UUID id) {
        goalRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndProfileId(UUID id, UUID profileId) {
        return goalRepository.existsByIdAndProfile_UserId(id, profileId);
    }
}
