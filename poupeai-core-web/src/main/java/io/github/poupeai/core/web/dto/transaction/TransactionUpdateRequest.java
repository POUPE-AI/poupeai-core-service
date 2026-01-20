package io.github.poupeai.core.web.dto.transaction;

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
public class TransactionUpdateRequest {
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String description;

    @Positive(message = "O valor deve ser maior que zero.")
    private BigDecimal amount;

    private LocalDate transactionDate;

    private UUID categoryId;

    @Size(max = 255, message = "A chave deve ter no máximo 255 caracteres.")
    private String attachmentKey;

    @Size(max = 100, message = "O ID do extrato deve ter no máximo 100 caracteres.")
    private String originalStatementId;

    private String originalStatementDescription;
}
