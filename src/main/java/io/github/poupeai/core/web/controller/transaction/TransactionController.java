package io.github.poupeai.core.web.controller.transaction;

import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.TransactionServicePort;
import io.github.poupeai.core.web.dto.common.PageResponse;
import io.github.poupeai.core.web.dto.transaction.TransactionRequest;
import io.github.poupeai.core.web.dto.transaction.TransactionResponse;
import io.github.poupeai.core.web.dto.transaction.TransactionUpdateRequest;
import io.github.poupeai.core.web.mapper.transaction.TransactionControllerMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Gerenciamento de Transações")
public class TransactionController {
    private final TransactionServicePort transactionServicePort;
    private final TransactionControllerMapper transactionMapper;

    @GetMapping
    @Operation(summary = "Listar transações", description = "Retorna todas as transações do usuário", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<PageResponse<TransactionResponse>> getTransactions(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID purchaseGroupUuid,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "description") String sortBy
    ) {
        TransactionFilter filter = TransactionFilter.builder()
                .page(page)
                .size(size)
                .type(type)
                .categoryId(categoryId)
                .purchaseGroupUuid(purchaseGroupUuid)
                .sortDirection(sortDirection)
                .sortBy(sortBy)
                .build();

        PageDomain<Transaction> pageResult = transactionServicePort.search(UUID.fromString(userId), filter);

        var responseContent = transactionMapper.toResponseList(pageResult.getContent());

        PageResponse<TransactionResponse> response =  PageResponse.<TransactionResponse>builder()
                .content(responseContent)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter transação por ID", description = "Retorna detalhes de uma transação específica", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id) {

        UUID profileId = UUID.fromString(userId);
        Transaction transaction = transactionServicePort.findByIdAndProfileId(id, profileId);
        return ResponseEntity.ok(transactionMapper.toResponse(transaction));
    }

    @PostMapping
    @Operation(summary = "Criar transação", description = "Cria uma nova transação. Transações parceladas geram múltiplas transações automaticamente.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> createTransaction(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @RequestBody @Valid TransactionRequest request) {

        UUID profileId = UUID.fromString(userId);
        Transaction transaction = transactionMapper.toDomain(request, profileId);
        Transaction savedTransaction = transactionServicePort.create(transaction);

        return ResponseEntity.ok(transactionMapper.toResponse(savedTransaction));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar transação", description = "Atualiza os dados de uma transação existente.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id,
            @RequestBody @Valid TransactionUpdateRequest request) {

        UUID profileId = UUID.fromString(userId);
        Transaction transaction = transactionServicePort.findByIdAndProfileId(id, profileId);
        transactionMapper.updateDomainFromDto(request, transaction);
        Transaction updatedTransaction = transactionServicePort.update(transaction, profileId);

        return ResponseEntity.ok(transactionMapper.toResponse(updatedTransaction));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar transação", description = "Deleta uma transação específica. Se a transação for parcelada, todas as parcelas serão deletadas.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id) {

        UUID profileId = UUID.fromString(userId);
        transactionServicePort.delete(id, profileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/receipt", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de comprovante", description = "Realiza o upload do comprovante para uma transação.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> uploadReceipt(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {

        UUID profileId = UUID.fromString(userId);
        Transaction transaction = transactionServicePort.uploadReceipt(
                id,
                profileId,
                file.getInputStream(),
                file.getContentType(),
                file.getSize());

        return ResponseEntity.ok(transactionMapper.toResponse(transaction));
    }

    @DeleteMapping("/{id}/receipt")
    @Operation(summary = "Remover comprovante", description = "Remove o comprovante de uma transação.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> deleteReceipt(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id) {

        UUID profileId = UUID.fromString(userId);
        Transaction transaction = transactionServicePort.deleteReceipt(id, profileId);

        return ResponseEntity.ok(transactionMapper.toResponse(transaction));
    }
}
