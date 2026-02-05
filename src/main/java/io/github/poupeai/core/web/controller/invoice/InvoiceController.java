package io.github.poupeai.core.web.controller.invoice;

import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceFilter;
import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.port.business.InvoicePaymentServicePort;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.web.dto.common.PageResponse;
import io.github.poupeai.core.web.dto.invoice.InvoiceResponse;
import io.github.poupeai.core.web.dto.invoicepayment.InvoicePaymentRequest;
import io.github.poupeai.core.web.dto.invoicepayment.InvoicePaymentResponse;
import io.github.poupeai.core.web.mapper.invoice.InvoiceControllerMapper;
import io.github.poupeai.core.web.mapper.invoicepayment.InvoicePaymentControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Endpoints para gerenciamento de faturas")
public class InvoiceController {
    private final InvoiceServicePort invoiceServicePort;
    private final InvoicePaymentServicePort invoicePaymentServicePort;
    private final InvoiceControllerMapper invoiceMapper;
    private final InvoicePaymentControllerMapper invoicePaymentMapper;

    @GetMapping
    @Operation(summary = "Listar faturas", description = "Lista todas as faturas do usuário")
    public ResponseEntity<PageResponse<InvoiceResponse>> list(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID creditCardId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "dueDate") String sortBy) {

        UUID userId = UUID.fromString(userIdStr);
        InvoiceFilter filter = InvoiceFilter.builder()
                .page(page).size(size)
                .creditCardId(creditCardId).status(status)
                .month(month).year(year)
                .sortDirection(sortDirection).sortBy(sortBy)
                .build();

        PageDomain<Invoice> pageResult = invoiceServicePort.search(userId, filter);

        return ResponseEntity.ok(PageResponse.<InvoiceResponse>builder()
                .content(invoiceMapper.toResponseList(pageResult.getContent()))
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fatura por ID", description = "Retorna os detalhes de uma fatura específica")
    public ResponseEntity<InvoiceResponse> getById(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id
    ) {
        UUID profileId = UUID.fromString(userId);
        Invoice invoice = invoiceServicePort.findById(id, profileId);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar fatura", description = "Remove uma fatura")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id
    ) {
        log.info("Deletando fatura {} para usuário {}", id, userId);
        UUID profileId = UUID.fromString(userId);
        invoiceServicePort.deleteInvoice(id, profileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    @Operation(
        summary = "Registrar pagamento de fatura",
        description = "Registra um pagamento de fatura, criando uma transação de saída na conta bancária especificada"
    )
    public ResponseEntity<InvoicePaymentResponse> registerPayment(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id,
            @Valid @RequestBody InvoicePaymentRequest request
    ) {
        log.info("Registrando pagamento de fatura {} para usuário {}", id, userId);
        
        UUID profileId = UUID.fromString(userId);
        
        InvoicePayment payment = invoicePaymentServicePort.registerPayment(
                id,
                request.getBankAccountId(),
                request.getAmount(),
                profileId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(invoicePaymentMapper.toResponse(payment));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    @Operation(
        summary = "Deletar pagamento e reabrir fatura",
        description = "Remove o pagamento, deleta a transação bancária associada e reverte o status da fatura"
    )
    public ResponseEntity<Void> deletePayment(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @PathVariable UUID id,
            @PathVariable Long paymentId
    ) {
        log.info("Deletando pagamento {} da fatura {} para usuário {}", paymentId, id, userId);
        
        UUID profileId = UUID.fromString(userId);
        invoicePaymentServicePort.deletePayment(paymentId, profileId);
        
        return ResponseEntity.noContent().build();
    }
}
