package io.github.poupeai.core.web.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class TransactionRequest {
    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String description;

    @NotNull(message = "O valor é obrigatório.")
    @Positive(message = "O valor deve ser maior que zero.")
    private BigDecimal amount;

    @NotNull(message = "A data da transação é obrigatória.")
    private LocalDate transactionDate;

    private UUID bankAccountId;

    private UUID creditCardId;

    @NotNull(message = "A categoria é obrigatória.")
    private UUID categoryId;

    @Size(max = 255, message = "A chave deve ter no máximo 255 caracteres.")
    private String attachmentKey;

    private Boolean isInstallment;

    private Integer totalInstallments;

    @Size(max = 100, message = "O ID do extrato deve ter no máximo 100 caracteres.")
    private String originalStatementId;

    private String originalStatementDescription;
}
