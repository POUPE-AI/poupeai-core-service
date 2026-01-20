package io.github.poupeai.core.domain.model;

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
public class Invoice {
    private UUID id;
    private UUID creditCardId;
    private Integer month;
    private Integer year;
    private LocalDate closingDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private InvoiceStatus status;
    private Boolean dueSoonNotificationSent;
    private Boolean overdueNotificationSent;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
