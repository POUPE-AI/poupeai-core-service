package io.github.poupeai.core.web.controller.transaction;

import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.port.business.TransactionServicePort;
import io.github.poupeai.core.web.dto.transaction.InternalCreateTransactionRequest;
import io.github.poupeai.core.web.mapper.transaction.TransactionControllerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal/transactions")
@RequiredArgsConstructor
@Tag(name = "Internal Transactions", description = "Endpoints internos para gestão de transações (System-to-System)")
public class InternalTransactionController {

    private final TransactionServicePort service;
    private final TransactionControllerMapper mapper;

    @PostMapping("/batch")
    @Operation(summary = "Criar transações em lote", description = "Recebe lista de transações do Ingestion Service.")
    public ResponseEntity<Void> createBatch(@RequestBody @Valid List<InternalCreateTransactionRequest> requests) {
        List<Transaction> transactions = mapper.toDomainListFromInternal(requests);
        service.createBatch(transactions);

        return ResponseEntity.ok().build();
    }
}