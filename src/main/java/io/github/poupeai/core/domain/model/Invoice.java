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

    public void updateStatusFromDates(LocalDate referenceDate) {
        if (paidAmount != null && totalAmount != null && paidAmount.compareTo(totalAmount) >= 0 && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PAID;
            return;
        }

        if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0 && paidAmount.compareTo(totalAmount) < 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        } else if (referenceDate.isAfter(dueDate)) {
            this.status = InvoiceStatus.OVERDUE;
        } else if (referenceDate.isAfter(closingDate)) {
            this.status = InvoiceStatus.CLOSED;
        } else {
            this.status = InvoiceStatus.OPEN;
        }
    }
}
