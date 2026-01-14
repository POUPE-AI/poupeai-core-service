package io.github.poupeai.core.web.dto.bankaccount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountRequest {
    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
    private String name;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    private String description;

    @PositiveOrZero(message = "O saldo inicial não pode ser negativo.")
    private BigDecimal initialBalance;

    private Boolean isDefault;

    private Long institutionId;
}
