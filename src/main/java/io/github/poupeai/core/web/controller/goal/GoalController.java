package io.github.poupeai.core.web.controller.goal;

import io.github.poupeai.core.domain.model.Goal;
import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.domain.port.business.GoalDepositServicePort;
import io.github.poupeai.core.domain.port.business.GoalServicePort;
import io.github.poupeai.core.web.dto.goal.GoalDepositRequest;
import io.github.poupeai.core.web.dto.goal.GoalDepositResponse;
import io.github.poupeai.core.web.dto.goal.GoalRequest;
import io.github.poupeai.core.web.dto.goal.GoalResponse;
import io.github.poupeai.core.web.dto.goal.GoalUpdateRequest;
import io.github.poupeai.core.web.mapper.goal.GoalControllerMapper;
import io.github.poupeai.core.web.mapper.goal.GoalDepositControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Tag(name = "Metas", description = "Gerenciamento de Metas")
public class GoalController {
    private final GoalServicePort goalServicePort;
    private final GoalDepositServicePort goalDepositServicePort;
    private final GoalControllerMapper goalMapper;
    private final GoalDepositControllerMapper goalDepositMapper;

    @GetMapping
    @Operation(
        summary = "Obter minhas metas",
        description = "Retorna todas as metas do usuário",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<List<GoalResponse>> getGoals(
        @Parameter(hidden = true) @CurrentUserId String userId) {

        List<Goal> goals = goalServicePort.findAllByProfileId(UUID.fromString(userId));
        return ResponseEntity.ok(goalMapper.toResponseList(goals));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obter meta por ID",
        description = "Retorna detalhes de uma meta específica",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<GoalResponse> getGoalById(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        Goal goal = goalServicePort.findByIdAndProfileId(id, UUID.fromString(userId));
        return ResponseEntity.ok(goalMapper.toResponse(goal));
    }

    @PostMapping
    @Operation(
        summary = "Criar meta",
        description = "Cria uma nova meta",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<GoalResponse> createGoal(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @RequestBody @Valid GoalRequest request) {

        Goal goal = goalMapper.toDomain(request, UUID.fromString(userId));
        Goal savedGoal = goalServicePort.create(goal);

        return ResponseEntity.ok(goalMapper.toResponse(savedGoal));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Atualizar meta",
        description = "Atualiza os dados de uma meta existente",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<GoalResponse> updateGoal(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id,
        @RequestBody @Valid GoalUpdateRequest request) {

        Goal goal = goalServicePort.findByIdAndProfileId(id, UUID.fromString(userId));
        goalMapper.updateDomainFromDto(request, goal);
        Goal updatedGoal = goalServicePort.update(goal);

        return ResponseEntity.ok(goalMapper.toResponse(updatedGoal));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deletar meta",
        description = "Deleta uma meta específica",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<Void> deleteGoal(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        goalServicePort.delete(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposits")
    @Operation(
        summary = "Adicionar depósito",
        description = "Adiciona um depósito a uma meta",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<GoalDepositResponse> addDeposit(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id,
        @RequestBody @Valid GoalDepositRequest request) {

        GoalDeposit deposit = goalDepositMapper.toDomain(request, id);
        GoalDeposit savedDeposit = goalDepositServicePort.create(deposit, UUID.fromString(userId));

        return ResponseEntity.ok(goalDepositMapper.toResponse(savedDeposit));
    }

    @GetMapping("/{id}/deposits")
    @Operation(
        summary = "Obter depósitos da meta",
        description = "Retorna todos os depósitos de uma meta",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<List<GoalDepositResponse>> getDeposits(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        List<GoalDeposit> deposits = goalDepositServicePort.findAllByGoalId(id, UUID.fromString(userId));
        return ResponseEntity.ok(goalDepositMapper.toResponseList(deposits));
    }

    @DeleteMapping("/{goal_id}/deposits/{deposit_id}")
    @Operation(
        summary = "Deletar depósito",
        description = "Deleta um depósito de uma meta",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<Void> deleteDeposit(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable("goal_id") UUID goalId,
        @PathVariable("deposit_id") UUID depositId) {

        goalDepositServicePort.delete(goalId, depositId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
