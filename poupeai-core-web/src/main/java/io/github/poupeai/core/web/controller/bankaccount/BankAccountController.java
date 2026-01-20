package io.github.poupeai.core.web.controller.bankaccount;

import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.port.business.BankAccountServicePort;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountRequest;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountResponse;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountUpdateRequest;
import io.github.poupeai.core.web.mapper.bankaccount.BankAccountControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@RequiredArgsConstructor
@Tag(name = "Contas Bancárias", description = "Gerenciamento de Contas Bancárias")
public class BankAccountController {
    private final BankAccountServicePort bankAccountServicePort;
    private final BankAccountControllerMapper bankAccountMapper;

    @GetMapping
    @Operation(
        summary = "Obter minhas contas bancárias",
        description = "Retorna todas as contas bancárias do usuário",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<List<BankAccountResponse>> getBankAccounts(
        @Parameter(hidden = true) @CurrentUserId String userId) {

        UUID profileId = UUID.fromString(userId);
        List<BankAccount> bankAccounts = bankAccountServicePort.findAllByProfileId(profileId);
        List<BankAccountResponse> responses = bankAccountMapper.toResponseList(bankAccounts);
        
        for (int i = 0; i < responses.size(); i++) {
            responses.get(i).setCurrentBalance(
                bankAccountServicePort.calculateCurrentBalance(bankAccounts.get(i).getId(), profileId)
            );
        }
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obter conta bancária por ID",
        description = "Retorna detalhes de uma conta bancária específica",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<BankAccountResponse> getBankAccountById(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        UUID profileId = UUID.fromString(userId);
        BankAccount bankAccount = bankAccountServicePort.findByIdAndProfileId(id, profileId);
        BankAccountResponse response = bankAccountMapper.toResponse(bankAccount);
        response.setCurrentBalance(bankAccountServicePort.calculateCurrentBalance(id, profileId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
        summary = "Criar conta bancária",
        description = "Cria uma nova conta bancária",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<BankAccountResponse> createBankAccount(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @RequestBody @Valid BankAccountRequest request) {

        BankAccount bankAccount = bankAccountMapper.toDomain(request, UUID.fromString(userId));
        BankAccount savedBankAccount = bankAccountServicePort.create(bankAccount);

        return ResponseEntity.ok(bankAccountMapper.toResponse(savedBankAccount));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Atualizar conta bancária",
        description = "Atualiza os dados de uma conta bancária existente",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<BankAccountResponse> updateBankAccount(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id, @RequestBody @Valid BankAccountUpdateRequest request) {

        BankAccount bankAccount = bankAccountServicePort.findByIdAndProfileId(id, UUID.fromString(userId));
        bankAccountMapper.updateDomainFromDto(request, bankAccount);
        BankAccount updatedBankAccount = bankAccountServicePort.update(bankAccount, UUID.fromString(userId));

        return ResponseEntity.ok(bankAccountMapper.toResponse(updatedBankAccount));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deletar conta bancária",
        description = "Deleta uma conta bancária específica",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<Void> deleteBankAccount(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        bankAccountServicePort.delete(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
