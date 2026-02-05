package io.github.poupeai.core.web.dto.creditcard;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import io.github.poupeai.core.web.dto.institution.InstitutionSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardResponse {
    private UUID id;
    private String name;
    private BigDecimal creditLimit;
    private BigDecimal usedCreditLimit;
    private Integer closingDay;
    private Integer dueDay;
    private InstitutionSummary institution;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
