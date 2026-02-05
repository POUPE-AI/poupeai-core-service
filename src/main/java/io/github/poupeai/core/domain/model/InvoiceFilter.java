package io.github.poupeai.core.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InvoiceFilter {
    private int page;
    private int size;
    private UUID creditCardId;
    private Integer month;
    private Integer year;
    private InvoiceStatus status;
    private String sortDirection;
    private String sortBy;
}
