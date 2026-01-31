package io.github.poupeai.core.web.dto.transaction;

import io.github.poupeai.core.domain.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class InternalCreateTransactionRequest {
    @NotNull(message = "Profile ID é obrigatório")
    private UUID profileId;

    @NotNull(message = "Bank Account ID é obrigatório")
    private UUID bankAccountId;

    private String description;

    @Positive
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

    private LocalDate date;

    private UUID categoryId;

    private String originalStatementId;
}
