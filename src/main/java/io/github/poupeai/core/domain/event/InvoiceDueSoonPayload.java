package io.github.poupeai.core.domain.event;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class InvoiceDueSoonPayload {
    private String creditCard;
    private Integer month;
    private Integer year;
    private LocalDate dueDate;
    private BigDecimal amount;
    private String invoiceDeepLink;
}
