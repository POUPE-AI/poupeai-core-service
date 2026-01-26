package io.github.poupeai.core.web.controller.goal;

import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.domain.port.business.GoalDepositServicePort;
import io.github.poupeai.core.domain.port.business.GoalServicePort;
import io.github.poupeai.core.web.dto.goal.*;
import io.github.poupeai.core.web.mapper.goal.GoalControllerMapper;
import io.github.poupeai.core.web.mapper.goal.GoalDepositControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalControllerTest {

    @Mock
    private GoalServicePort goalServicePort;

    @Mock
    private GoalDepositServicePort goalDepositServicePort;

    @Mock
    private GoalControllerMapper goalMapper;

    @Mock
    private GoalDepositControllerMapper goalDepositMapper;

    @InjectMocks
    private GoalController goalController;

    @Test
    @DisplayName("Should get goals successfully")
    void shouldGetGoalsSuccessfully() {
        String userId = UUID.randomUUID().toString();
        List<Goal> goals = List.of(
            Goal.builder().name("Emergency Fund").build(),
            Goal.builder().name("Vacation").build()
        );
        List<GoalResponse> responses = List.of(
            new GoalResponse(),
            new GoalResponse()
        );

        when(goalServicePort.findAllByProfileId(UUID.fromString(userId))).thenReturn(goals);
        when(goalMapper.toResponseList(goals)).thenReturn(responses);

        ResponseEntity<List<GoalResponse>> result = goalController.getGoals(userId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
        assertEquals(2, result.getBody().size());
    }

    @Test
    @DisplayName("Should get goal by id successfully when user is owner")
    void shouldGetGoalByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        Goal goal = Goal.builder()
                .id(goalId)
                .profileId(userId)
                .name("Emergency Fund")
                .build();
        GoalResponse response = new GoalResponse();

        when(goalServicePort.findByIdAndProfileId(goalId, userId)).thenReturn(goal);
        when(goalMapper.toResponse(goal)).thenReturn(response);

        ResponseEntity<GoalResponse> result = goalController.getGoalById(userId.toString(), goalId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should create goal successfully")
    void shouldCreateGoalSuccessfully() {
        String userId = UUID.randomUUID().toString();
        GoalRequest request = GoalRequest.builder()
                .name("New Goal")
                .goalAmount(BigDecimal.valueOf(10000))
                .targetDate(LocalDate.now().plusMonths(12))
                .build();
        Goal goal = new Goal();
        Goal savedGoal = Goal.builder()
                .id(UUID.randomUUID())
                .name("New Goal")
                .build();
        GoalResponse response = new GoalResponse();

        when(goalMapper.toDomain(eq(request), any(UUID.class))).thenReturn(goal);
        when(goalServicePort.create(goal)).thenReturn(savedGoal);
        when(goalMapper.toResponse(savedGoal)).thenReturn(response);

        ResponseEntity<GoalResponse> result = goalController.createGoal(userId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(goalServicePort).create(goal);
    }

    @Test
    @DisplayName("Should update goal successfully when user is owner")
    void shouldUpdateGoalSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .name("Updated Goal")
                .goalAmount(BigDecimal.valueOf(15000))
                .build();
        Goal goal = Goal.builder()
                .id(goalId)
                .profileId(userId)
                .name("Old Name")
                .build();
        GoalResponse response = new GoalResponse();

        when(goalServicePort.findByIdAndProfileId(goalId, userId)).thenReturn(goal);
        doNothing().when(goalMapper).updateDomainFromDto(eq(request), eq(goal));
        when(goalServicePort.update(goal)).thenReturn(goal);
        when(goalMapper.toResponse(goal)).thenReturn(response);

        ResponseEntity<GoalResponse> result = goalController.updateGoal(userId.toString(), goalId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(goalServicePort).update(goal);
    }

    @Test
    @DisplayName("Should delete goal successfully when user is owner")
    void shouldDeleteGoalSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        doNothing().when(goalServicePort).delete(goalId, userId);

        ResponseEntity<Void> result = goalController.deleteGoal(userId.toString(), goalId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(goalServicePort).delete(goalId, userId);
    }

    @Test
    @DisplayName("Should add deposit successfully")
    void shouldAddDepositSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        GoalDepositRequest request = GoalDepositRequest.builder()
                .depositAmount(BigDecimal.valueOf(500))
                .depositDate(LocalDate.now())
                .build();
        GoalDeposit deposit = new GoalDeposit();
        GoalDeposit savedDeposit = GoalDeposit.builder()
                .id(UUID.randomUUID())
                .goalId(goalId)
                .build();
        GoalDepositResponse response = new GoalDepositResponse();

        when(goalDepositMapper.toDomain(request, goalId)).thenReturn(deposit);
        when(goalDepositServicePort.create(deposit, userId)).thenReturn(savedDeposit);
        when(goalDepositMapper.toResponse(savedDeposit)).thenReturn(response);

        ResponseEntity<GoalDepositResponse> result = goalController.addDeposit(userId.toString(), goalId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(goalDepositServicePort).create(deposit, userId);
    }

    @Test
    @DisplayName("Should get deposits successfully")
    void shouldGetDepositsSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        List<GoalDeposit> deposits = List.of(
            GoalDeposit.builder().depositAmount(BigDecimal.valueOf(100)).build(),
            GoalDeposit.builder().depositAmount(BigDecimal.valueOf(200)).build()
        );
        List<GoalDepositResponse> responses = List.of(
            new GoalDepositResponse(),
            new GoalDepositResponse()
        );

        when(goalDepositServicePort.findAllByGoalId(goalId, userId)).thenReturn(deposits);
        when(goalDepositMapper.toResponseList(deposits)).thenReturn(responses);

        ResponseEntity<List<GoalDepositResponse>> result = goalController.getDeposits(userId.toString(), goalId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
        assertEquals(2, result.getBody().size());
    }

    @Test
    @DisplayName("Should delete deposit successfully")
    void shouldDeleteDepositSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        UUID depositId = UUID.randomUUID();

        doNothing().when(goalDepositServicePort).delete(goalId, depositId, userId);

        ResponseEntity<Void> result = goalController.deleteDeposit(userId.toString(), goalId, depositId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(goalDepositServicePort).delete(goalId, depositId, userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no goals")
    void shouldReturnEmptyListWhenNoGoals() {
        String userId = UUID.randomUUID().toString();

        when(goalServicePort.findAllByProfileId(UUID.fromString(userId))).thenReturn(List.of());
        when(goalMapper.toResponseList(List.of())).thenReturn(List.of());

        ResponseEntity<List<GoalResponse>> result = goalController.getGoals(userId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when goal has no deposits")
    void shouldReturnEmptyListWhenNoDeposits() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        when(goalDepositServicePort.findAllByGoalId(goalId, userId)).thenReturn(List.of());
        when(goalDepositMapper.toResponseList(List.of())).thenReturn(List.of());

        ResponseEntity<List<GoalDepositResponse>> result = goalController.getDeposits(userId.toString(), goalId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should create goal with target date")
    void shouldCreateGoalWithTargetDate() {
        String userId = UUID.randomUUID().toString();
        LocalDate targetDate = LocalDate.now().plusYears(1);
        GoalRequest request = GoalRequest.builder()
                .name("Long-term Goal")
                .goalAmount(BigDecimal.valueOf(50000))
                .targetDate(targetDate)
                .build();
        Goal goal = Goal.builder().targetDate(targetDate).build();
        Goal savedGoal = Goal.builder()
                .id(UUID.randomUUID())
                .targetDate(targetDate)
                .build();
        GoalResponse response = new GoalResponse();

        when(goalMapper.toDomain(eq(request), any(UUID.class))).thenReturn(goal);
        when(goalServicePort.create(goal)).thenReturn(savedGoal);
        when(goalMapper.toResponse(savedGoal)).thenReturn(response);

        ResponseEntity<GoalResponse> result = goalController.createGoal(userId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(goalServicePort).create(goal);
    }

    @Test
    @DisplayName("Should update goal marking as completed")
    void shouldUpdateGoalMarkingAsCompleted() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        LocalDate completedDate = LocalDate.now();
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .completedAt(completedDate)
                .build();
        Goal goal = Goal.builder()
                .id(goalId)
                .profileId(userId)
                .build();
        GoalResponse response = new GoalResponse();

        when(goalServicePort.findByIdAndProfileId(goalId, userId)).thenReturn(goal);
        doNothing().when(goalMapper).updateDomainFromDto(eq(request), eq(goal));
        when(goalServicePort.update(goal)).thenReturn(goal);
        when(goalMapper.toResponse(goal)).thenReturn(response);

        ResponseEntity<GoalResponse> result = goalController.updateGoal(userId.toString(), goalId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(goalMapper).updateDomainFromDto(request, goal);
    }

    @Test
    @DisplayName("Should add deposit with past date")
    void shouldAddDepositWithPastDate() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        LocalDate pastDate = LocalDate.now().minusDays(30);
        
        GoalDepositRequest request = GoalDepositRequest.builder()
                .depositAmount(BigDecimal.valueOf(100))
                .depositDate(pastDate)
                .build();

        GoalDeposit deposit = new GoalDeposit();
        GoalDeposit savedDeposit = GoalDeposit.builder()
                .id(UUID.randomUUID())
                .depositDate(pastDate)
                .build();
        GoalDepositResponse response = new GoalDepositResponse();

        when(goalDepositMapper.toDomain(request, goalId)).thenReturn(deposit);
        when(goalDepositServicePort.create(deposit, userId)).thenReturn(savedDeposit);
        when(goalDepositMapper.toResponse(savedDeposit)).thenReturn(response);

        ResponseEntity<GoalDepositResponse> result = goalController.addDeposit(userId.toString(), goalId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(goalDepositServicePort).create(deposit, userId);
    }
}
