package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.persistence.entity.GoalEntity;
import io.github.poupeai.core.persistence.entity.ProfileEntity;
import io.github.poupeai.core.persistence.mapper.GoalEntityMapper;
import io.github.poupeai.core.persistence.repository.GoalRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalRepositoryAdapterTest {

    @InjectMocks
    private GoalRepositoryAdapter adapter;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalEntityMapper goalMapper;

    @Mock
    private ProfileRepository profileRepository;

    @Test
    @DisplayName("Should persist goal and link profile when data is valid")
    void createShouldPersistWhenDataIsValid() {
        UUID profileId = UUID.randomUUID();
        Goal domain = Goal.builder()
                .profileId(profileId)
                .name("Emergency Fund")
                .goalAmount(BigDecimal.valueOf(10000))
                .build();

        GoalEntity entity = new GoalEntity();
        ProfileEntity profileProxy = new ProfileEntity();

        when(goalMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(goalRepository.save(entity)).thenReturn(entity);
        when(goalMapper.toDomain(entity)).thenReturn(domain);

        Goal result = adapter.create(domain);

        assertNotNull(result);
        verify(goalRepository).save(entity);
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update goal fields when goal exists")
    void updateShouldUpdateFieldsWhenGoalExists() {
        UUID goalId = UUID.randomUUID();
        Goal domain = Goal.builder()
                .id(goalId)
                .profileId(UUID.randomUUID())
                .name("Updated Goal")
                .goalAmount(BigDecimal.valueOf(15000))
                .targetDate(LocalDate.now().plusMonths(6))
                .build();

        GoalEntity existingEntity = new GoalEntity();
        ProfileEntity profileProxy = new ProfileEntity();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingEntity));
        when(profileRepository.getReferenceById(any())).thenReturn(profileProxy);
        when(goalRepository.save(existingEntity)).thenReturn(existingEntity);
        when(goalMapper.toDomain(existingEntity)).thenReturn(domain);

        Goal result = adapter.update(domain);

        assertNotNull(result);
        assertEquals("Updated Goal", existingEntity.getName());
        assertEquals(BigDecimal.valueOf(15000), existingEntity.getGoalAmount());
        assertNotNull(existingEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when goal does not exist during update")
    void updateShouldThrowNotFoundWhenGoalDoesNotExist() {
        UUID goalId = UUID.randomUUID();
        Goal domain = Goal.builder().id(goalId).build();

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adapter.update(domain));
        verify(goalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return goal when found by ID and Profile ID")
    void findByIdShouldReturnGoalWhenFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        GoalEntity entity = new GoalEntity();
        Goal domain = new Goal();

        when(goalRepository.findByIdAndProfile_UserId(id, profileId)).thenReturn(Optional.of(entity));
        when(goalMapper.toDomain(entity)).thenReturn(domain);

        Optional<Goal> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should return empty optional when goal is not found by ID and Profile ID")
    void findByIdShouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(goalRepository.findByIdAndProfile_UserId(id, profileId)).thenReturn(Optional.empty());

        Optional<Goal> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return list of goals mapped to domain")
    void findAllShouldReturnList() {
        UUID profileId = UUID.randomUUID();
        List<GoalEntity> entities = List.of(new GoalEntity(), new GoalEntity());
        List<Goal> domains = List.of(new Goal(), new Goal());

        when(goalRepository.findAllByProfile_UserId(profileId)).thenReturn(entities);
        when(goalMapper.toDomainList(entities)).thenReturn(domains);

        List<Goal> result = adapter.findAllByProfileId(profileId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when profile has no goals")
    void findAllShouldReturnEmptyListWhenNoGoals() {
        UUID profileId = UUID.randomUUID();
        when(goalRepository.findAllByProfile_UserId(profileId)).thenReturn(List.of());
        when(goalMapper.toDomainList(List.of())).thenReturn(List.of());

        List<Goal> result = adapter.findAllByProfileId(profileId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should call delete by ID in repository")
    void deleteShouldCallRepository() {
        UUID id = UUID.randomUUID();
        adapter.delete(id);
        verify(goalRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should return true when goal exists for the given profile")
    void existsByIdShouldReturnTrue() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(goalRepository.existsByIdAndProfile_UserId(id, profileId)).thenReturn(true);

        assertTrue(adapter.existsByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should return false when goal does not exist for the given profile")
    void existsByIdShouldReturnFalse() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(goalRepository.existsByIdAndProfile_UserId(id, profileId)).thenReturn(false);

        assertFalse(adapter.existsByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should update goal with completed date")
    void updateShouldSetCompletedDate() {
        UUID goalId = UUID.randomUUID();
        LocalDate completedDate = LocalDate.now();
        Goal domain = Goal.builder()
                .id(goalId)
                .profileId(UUID.randomUUID())
                .name("Completed Goal")
                .goalAmount(BigDecimal.valueOf(5000))
                .completedAt(completedDate)
                .build();

        GoalEntity existingEntity = new GoalEntity();
        ProfileEntity profileProxy = new ProfileEntity();

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingEntity));
        when(profileRepository.getReferenceById(any())).thenReturn(profileProxy);
        when(goalRepository.save(existingEntity)).thenReturn(existingEntity);
        when(goalMapper.toDomain(existingEntity)).thenReturn(domain);

        Goal result = adapter.update(domain);

        assertNotNull(result);
        assertEquals(completedDate, existingEntity.getCompletedAt());
    }

    @Test
    @DisplayName("Should create goal with target date")
    void createShouldSetTargetDate() {
        UUID profileId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.now().plusYears(1);
        Goal domain = Goal.builder()
                .profileId(profileId)
                .name("Long-term Goal")
                .goalAmount(BigDecimal.valueOf(50000))
                .targetDate(targetDate)
                .build();

        GoalEntity entity = GoalEntity.builder().targetDate(targetDate).build();
        ProfileEntity profileProxy = new ProfileEntity();

        when(goalMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(goalRepository.save(entity)).thenReturn(entity);
        when(goalMapper.toDomain(entity)).thenReturn(domain);

        Goal result = adapter.create(domain);

        assertNotNull(result);
        assertEquals(targetDate, entity.getTargetDate());
    }
}
