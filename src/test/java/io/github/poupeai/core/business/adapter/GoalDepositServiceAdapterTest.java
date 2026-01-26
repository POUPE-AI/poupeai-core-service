package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.domain.port.persistence.GoalDepositRepositoryPort;
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
class GoalDepositServiceAdapterTest {

    @Mock
    private GoalDepositRepositoryPort goalDepositRepositoryPort;

    @Mock
    private GoalRepositoryPort goalRepositoryPort;

    @InjectMocks
    private GoalDepositServiceAdapter goalDepositServiceAdapter;

    @Test
    @DisplayName("Should create deposit successfully when goal exists")
    void shouldCreateDepositSuccessfully() {
        UUID goalId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        GoalDeposit deposit = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(500))
                .depositDate(LocalDate.now())
                .build();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(true);
        when(goalDepositRepositoryPort.create(deposit)).thenReturn(deposit);

        GoalDeposit result = goalDepositServiceAdapter.create(deposit, profileId);

        assertNotNull(result);
        assertEquals(deposit.getDepositAmount(), result.getDepositAmount());
        verify(goalRepositoryPort).existsByIdAndProfileId(goalId, profileId);
        verify(goalDepositRepositoryPort).create(deposit);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating deposit for non-existent goal")
    void shouldThrowExceptionWhenCreatingDepositForNonExistentGoal() {
        UUID goalId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        GoalDeposit deposit = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(500))
                .build();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
            () -> goalDepositServiceAdapter.create(deposit, profileId));
        verify(goalDepositRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should find all deposits by goal id successfully")
    void shouldFindAllDepositsByGoalId() {
        UUID goalId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        List<GoalDeposit> deposits = List.of(
            GoalDeposit.builder().depositAmount(BigDecimal.valueOf(100)).build(),
            GoalDeposit.builder().depositAmount(BigDecimal.valueOf(200)).build()
        );

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(true);
        when(goalDepositRepositoryPort.findAllByGoalId(goalId)).thenReturn(deposits);

        List<GoalDeposit> result = goalDepositServiceAdapter.findAllByGoalId(goalId, profileId);

        assertEquals(2, result.size());
        verify(goalRepositoryPort).existsByIdAndProfileId(goalId, profileId);
        verify(goalDepositRepositoryPort).findAllByGoalId(goalId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding deposits for non-existent goal")
    void shouldThrowExceptionWhenFindingDepositsForNonExistentGoal() {
        UUID goalId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
            () -> goalDepositServiceAdapter.findAllByGoalId(goalId, profileId));
        verify(goalDepositRepositoryPort, never()).findAllByGoalId(any());
    }

    @Test
    @DisplayName("Should return empty list when goal has no deposits")
    void shouldReturnEmptyListWhenGoalHasNoDeposits() {
        UUID goalId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(true);
        when(goalDepositRepositoryPort.findAllByGoalId(goalId)).thenReturn(List.of());

        List<GoalDeposit> result = goalDepositServiceAdapter.findAllByGoalId(goalId, profileId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should delete deposit successfully when goal and deposit exist")
    void shouldDeleteDepositSuccessfully() {
        UUID goalId = UUID.randomUUID();
        UUID depositId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        GoalDeposit deposit = GoalDeposit.builder().id(depositId).goalId(goalId).build();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(true);
        when(goalDepositRepositoryPort.findByIdAndGoalId(depositId, goalId)).thenReturn(Optional.of(deposit));
        doNothing().when(goalDepositRepositoryPort).delete(depositId);

        goalDepositServiceAdapter.delete(goalId, depositId, profileId);

        verify(goalRepositoryPort).existsByIdAndProfileId(goalId, profileId);
        verify(goalDepositRepositoryPort).findByIdAndGoalId(depositId, goalId);
        verify(goalDepositRepositoryPort).delete(depositId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting deposit from non-existent goal")
    void shouldThrowExceptionWhenDeletingDepositFromNonExistentGoal() {
        UUID goalId = UUID.randomUUID();
        UUID depositId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
            () -> goalDepositServiceAdapter.delete(goalId, depositId, profileId));
        verify(goalDepositRepositoryPort, never()).findByIdAndGoalId(any(), any());
        verify(goalDepositRepositoryPort, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent deposit")
    void shouldThrowExceptionWhenDeletingNonExistentDeposit() {
        UUID goalId = UUID.randomUUID();
        UUID depositId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(true);
        when(goalDepositRepositoryPort.findByIdAndGoalId(depositId, goalId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> goalDepositServiceAdapter.delete(goalId, depositId, profileId));
        verify(goalDepositRepositoryPort, never()).delete(any());
    }

    @Test
    @DisplayName("Should create deposit with specific date")
    void shouldCreateDepositWithSpecificDate() {
        UUID goalId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        LocalDate specificDate = LocalDate.of(2025, 1, 15);
        GoalDeposit deposit = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(1000))
                .depositDate(specificDate)
                .build();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(true);
        when(goalDepositRepositoryPort.create(deposit)).thenReturn(deposit);

        GoalDeposit result = goalDepositServiceAdapter.create(deposit, profileId);

        assertNotNull(result);
        assertEquals(specificDate, result.getDepositDate());
    }

    @Test
    @DisplayName("Should create multiple deposits for same goal")
    void shouldCreateMultipleDepositsForSameGoal() {
        UUID goalId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        GoalDeposit deposit1 = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(100))
                .depositDate(LocalDate.now())
                .build();
        GoalDeposit deposit2 = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(200))
                .depositDate(LocalDate.now())
                .build();

        when(goalRepositoryPort.existsByIdAndProfileId(goalId, profileId)).thenReturn(true);
        when(goalDepositRepositoryPort.create(deposit1)).thenReturn(deposit1);
        when(goalDepositRepositoryPort.create(deposit2)).thenReturn(deposit2);

        GoalDeposit result1 = goalDepositServiceAdapter.create(deposit1, profileId);
        GoalDeposit result2 = goalDepositServiceAdapter.create(deposit2, profileId);

        assertNotNull(result1);
        assertNotNull(result2);
        verify(goalDepositRepositoryPort, times(2)).create(any());
    }
}
