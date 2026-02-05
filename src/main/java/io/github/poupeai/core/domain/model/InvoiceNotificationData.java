package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceNotificationData {
    private UUID invoiceId;
    private UUID creditCardId;
    private String creditCardName;
    private Integer month;
    private Integer year;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    
    private UUID userId;
    private String userEmail;
    private String userName;
    
    public BigDecimal getAmountDue() {
        return totalAmount.subtract(paidAmount);
    }
}
