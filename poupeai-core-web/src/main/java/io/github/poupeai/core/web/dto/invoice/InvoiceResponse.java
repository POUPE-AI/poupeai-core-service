package io.github.poupeai.core.web.dto.invoice;

import io.github.poupeai.core.domain.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private UUID id;
    private UUID creditCardId;
    private Integer month;
    private Integer year;
    private LocalDate closingDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private InvoiceStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
