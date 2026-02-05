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
public class GoalDepositResponse {
    private UUID id;
    private UUID goalId;
    private BigDecimal depositAmount;
    private LocalDate depositDate;
    private OffsetDateTime createdAt;
}
