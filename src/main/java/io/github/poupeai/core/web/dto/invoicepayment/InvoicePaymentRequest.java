package io.github.poupeai.core.web.dto.invoicepayment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentRequest {
    @NotNull(message = "O ID da conta bancária é obrigatório.")
    private UUID bankAccountId;

    @NotNull(message = "O valor do pagamento é obrigatório.")
    @Positive(message = "O valor do pagamento deve ser maior que zero.")
    private BigDecimal amount;
}
