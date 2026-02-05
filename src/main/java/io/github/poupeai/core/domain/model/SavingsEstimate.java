package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsEstimate {
    private BigDecimal estimatedSavings;
    private BigDecimal savingsPercentage;
    private String message;
    private String comparisonPeriod;
}
