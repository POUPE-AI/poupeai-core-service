package io.github.poupeai.core.web.dto.creditcard;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardUpdateRequest {
    @Size(max = 50) private String name;
    @PositiveOrZero(message = "O limite de crédito não pode ser negativo.") private BigDecimal creditLimit;
    @NotNull @Min(1) @Max(31) private Integer closingDay;
    @NotNull @Min(1) @Max(31) private Integer dueDay;
    private Long institutionId;
}
