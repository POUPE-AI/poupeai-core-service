package io.github.poupeai.core.web.dto.goal;

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
public class GoalUpdateRequest {
    private String name;
    private String description;
    private String colorHex;
    private BigDecimal initialBalance;

    @Positive
    private BigDecimal goalAmount;
    
    private LocalDate targetDate;
    
    private LocalDate completedAt;
}
