package io.github.poupeai.core.web.dto.bankaccount;

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
public class BankAccountResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private Boolean isDefault;
    private Long institutionId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
