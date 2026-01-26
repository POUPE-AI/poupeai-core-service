package io.github.poupeai.core.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TransactionFilter {
    private int page;
    private int size;
    private TransactionType type;
    private UUID categoryId;
    private UUID purchaseGroupUuid;
    private String sortDirection;
    private String sortBy;
}
