package io.github.poupeai.core.web.dto.transaction;

import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.web.dto.category.CategorySummary;
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
public class TransactionResponse {
    private UUID id;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDate transactionDate;

    private UUID bankAccountId;
    private UUID creditCardId;
    private CategorySummary category;

    private UUID invoiceId;
    private String attachmentKey;
    private String attachmentUrl;

    private Boolean isInstallment;
    private Integer installmentNumber;
    private Integer totalInstallments;
    private UUID purchaseGroupUuid;

    private String originalStatementId;
    private String originalStatementDescription;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
