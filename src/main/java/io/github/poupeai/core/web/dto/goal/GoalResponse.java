package io.github.poupeai.core.web.dto.goal;

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
public class GoalResponse {
    private UUID id;
    private String name;
    private String description;
    private String colorHex;
    private BigDecimal initialBalance;
    private BigDecimal goalAmount;
    private BigDecimal currentBalance;
    private BigDecimal percentageCompleted;
    private LocalDate targetDate;
    private LocalDate completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
