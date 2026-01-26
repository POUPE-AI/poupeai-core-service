package io.github.poupeai.core.domain.model;

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
public class Goal {
    private UUID id;
    private UUID profileId;
    private String name;
    private String description;
    private String colorHex;
    private BigDecimal initialBalance;
    private BigDecimal goalAmount;
    private BigDecimal currentBalance;
    private LocalDate targetDate;
    private LocalDate completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
