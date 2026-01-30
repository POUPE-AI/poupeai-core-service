package io.github.poupeai.core.web.controller.transaction;

import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.TransactionServicePort;
import io.github.poupeai.core.web.dto.common.PageResponse;
import io.github.poupeai.core.web.dto.transaction.CreateTransactionRequest;
import io.github.poupeai.core.web.dto.transaction.TransactionResponse;
import io.github.poupeai.core.web.dto.transaction.UpdateTransactionRequest;
import io.github.poupeai.core.web.mapper.transaction.TransactionControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Gerenciamento de Transações")
public class TransactionController {
    private final TransactionServicePort service;
    private final TransactionControllerMapper mapper;

    @GetMapping
    @Operation(summary = "Listar transações", description = "Retorna todas as transações do usuário", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<PageResponse<TransactionResponse>> list(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID purchaseGroupUuid,
            @RequestParam(required = false) LocalDate transactionDateStart,
            @RequestParam(required = false) LocalDate transactionDateEnd,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "transactionDate") String sortBy) {

        UUID userId = UUID.fromString(userIdStr);

        if (transactionDateEnd == null) {
            transactionDateEnd = LocalDate.now();
        }

        TransactionFilter filter = TransactionFilter.builder()
                .page(page).size(size)
                .type(type).categoryId(categoryId)
                .purchaseGroupUuid(purchaseGroupUuid)
                .transactionDateStart(transactionDateStart)
                .transactionDateEnd(transactionDateEnd)
                .sortDirection(sortDirection).sortBy(sortBy)
                .build();

        PageDomain<Transaction> pageResult = service.search(userId, filter);

        return ResponseEntity.ok(PageResponse.<TransactionResponse>builder()
                .content(mapper.toResponseList(pageResult.getContent()))
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build());
    }

    @PostMapping
    @Operation(summary = "Criar transação", description = "Cria uma nova transação. Transações parceladas geram múltiplas transações automaticamente.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> create(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @RequestBody @Valid CreateTransactionRequest request) {

        Transaction domain = mapper.toDomain(request, UUID.fromString(userIdStr));
        Transaction saved = service.create(domain);
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna detalhes de uma transação específica", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> getById(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @PathVariable UUID id) {
        Transaction t = service.findByIdAndProfileId(id, UUID.fromString(userIdStr));
        return ResponseEntity.ok(mapper.toResponse(t));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar transação", description = "Atualiza os dados de uma transação existente.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> update(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateTransactionRequest request) {

        Transaction partial = mapper.toDomain(request, id);
        Transaction updated = service.update(partial, UUID.fromString(userIdStr));
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar transação", description = "Deleta uma transação específica. Se a transação for parcelada, todas as parcelas serão deletadas.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @PathVariable UUID id) {
        service.delete(id, UUID.fromString(userIdStr));
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de comprovante", description = "Realiza o upload do comprovante para uma transação.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> uploadReceipt(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {

        Transaction t = service.uploadReceipt(id, UUID.fromString(userIdStr),
                file.getInputStream(), file.getContentType(), file.getSize());
        return ResponseEntity.ok(mapper.toResponse(t));
    }

    @DeleteMapping("/{id}/receipt")
    @Operation(summary = "Remover comprovante", description = "Remove o comprovante de uma transação.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<TransactionResponse> deleteReceipt(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @PathVariable UUID id) {
        Transaction t = service.deleteReceipt(id, UUID.fromString(userIdStr));
        return ResponseEntity.ok(mapper.toResponse(t));
    }
}
