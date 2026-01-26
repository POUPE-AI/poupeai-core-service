package io.github.poupeai.core.web.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {
    @NotBlank
    private String name;
    
    private String description;

    @NotBlank
    private String colorHex;
    
    private BigDecimal initialBalance;

    @NotNull
    @Positive
    private BigDecimal goalAmount;
    
    private LocalDate targetDate;
}
