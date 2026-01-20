package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePayment {
    private Long id;
    private UUID invoiceId;
    private UUID paymentTransactionId;
    private BigDecimal amount;
    private OffsetDateTime createdAt;
}
