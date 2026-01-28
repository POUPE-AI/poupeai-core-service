package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {
    private UUID id;
    private UUID profileId;
    private Institution institution;
    private String name;
    private BigDecimal creditLimit;
    private BigDecimal usedCreditLimit;
    private Integer closingDay;
    private Integer dueDay;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
