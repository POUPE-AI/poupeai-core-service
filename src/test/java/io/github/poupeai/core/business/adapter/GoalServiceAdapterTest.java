package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.domain.port.persistence.GoalRepositoryPort;
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
class GoalServiceAdapterTest {

    @Mock
    private GoalRepositoryPort goalRepositoryPort;

    @InjectMocks
    private GoalServiceAdapter goalServiceAdapter;

    @Test
    @DisplayName("Should create goal successfully")
    void shouldCreateGoalSuccessfully() {
        Goal goal = Goal.builder()
                .profileId(UUID.randomUUID())
                .name("Emergency Fund")
                .goalAmount(BigDecimal.valueOf(10000))
                .targetDate(LocalDate.now().plusMonths(12))
                .build();

        when(goalRepositoryPort.create(goal)).thenReturn(goal);

        Goal result = goalServiceAdapter.create(goal);

        assertNotNull(result);
        assertEquals(goal.getName(), result.getName());
        assertEquals(goal.getGoalAmount(), result.getGoalAmount());
        verify(goalRepositoryPort).create(goal);
    }

    @Test
    @DisplayName("Should update goal successfully")
    void shouldUpdateGoalSuccessfully() {
        UUID id = UUID.randomUUID();
        Goal goal = Goal.builder()
                .id(id)
                .profileId(UUID.randomUUID())
                .name("Updated Goal")
                .goalAmount(BigDecimal.valueOf(15000))
                .build();

        when(goalRepositoryPort.update(goal)).thenReturn(goal);

        Goal result = goalServiceAdapter.update(goal);

        assertNotNull(result);
        assertEquals("Updated Goal", result.getName());
        verify(goalRepositoryPort).update(goal);
    }

    @Test
    @DisplayName("Should find goal by id successfully")
    void shouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        Goal goal = Goal.builder()
                .id(id)
                .profileId(profileId)
                .name("Vacation Fund")
                .build();

        when(goalRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.of(goal));

        Goal result = goalServiceAdapter.findByIdAndProfileId(id, profileId);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Vacation Fund", result.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when goal not found by id")
    void shouldThrowExceptionWhenGoalNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(goalRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> goalServiceAdapter.findByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should find all goals by profile id")
    void shouldFindAllByProfileId() {
        UUID profileId = UUID.randomUUID();
        List<Goal> goals = List.of(
            Goal.builder().name("Goal 1").build(),
            Goal.builder().name("Goal 2").build()
        );

        when(goalRepositoryPort.findAllByProfileId(profileId)).thenReturn(goals);

        List<Goal> result = goalServiceAdapter.findAllByProfileId(profileId);

        assertEquals(2, result.size());
        verify(goalRepositoryPort).findAllByProfileId(profileId);
    }

    @Test
    @DisplayName("Should return empty list when no goals found")
    void shouldReturnEmptyListWhenNoGoals() {
        UUID profileId = UUID.randomUUID();
        when(goalRepositoryPort.findAllByProfileId(profileId)).thenReturn(List.of());

        List<Goal> result = goalServiceAdapter.findAllByProfileId(profileId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should delete goal successfully")
    void shouldDeleteGoalSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(goalRepositoryPort.existsByIdAndProfileId(id, profileId)).thenReturn(true);
        doNothing().when(goalRepositoryPort).delete(id);

        goalServiceAdapter.delete(id, profileId);

        verify(goalRepositoryPort).existsByIdAndProfileId(id, profileId);
        verify(goalRepositoryPort).delete(id);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent goal")
    void shouldThrowExceptionWhenDeletingNonExistentGoal() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(goalRepositoryPort.existsByIdAndProfileId(id, profileId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
            () -> goalServiceAdapter.delete(id, profileId));
        verify(goalRepositoryPort, never()).delete(any());
    }

    @Test
    @DisplayName("Should create goal with completed date")
    void shouldCreateGoalWithCompletedDate() {
        Goal goal = Goal.builder()
                .profileId(UUID.randomUUID())
                .name("Completed Goal")
                .goalAmount(BigDecimal.valueOf(5000))
                .completedAt(LocalDate.now())
                .build();

        when(goalRepositoryPort.create(goal)).thenReturn(goal);

        Goal result = goalServiceAdapter.create(goal);

        assertNotNull(result);
        assertNotNull(result.getCompletedAt());
    }

    @Test
    @DisplayName("Should update goal marking as completed")
    void shouldUpdateGoalMarkingAsCompleted() {
        UUID id = UUID.randomUUID();
        Goal goal = Goal.builder()
                .id(id)
                .profileId(UUID.randomUUID())
                .name("Goal")
                .goalAmount(BigDecimal.valueOf(5000))
                .completedAt(LocalDate.now())
                .build();

        when(goalRepositoryPort.update(goal)).thenReturn(goal);

        Goal result = goalServiceAdapter.update(goal);

        assertNotNull(result);
        assertNotNull(result.getCompletedAt());
    }
}
