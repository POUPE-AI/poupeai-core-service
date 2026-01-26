package io.github.poupeai.core.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryFilter {
    private int page;
    private int size;
    private String name;
    private CategoryType type;
    private String sortDirection;
    private String sortBy;
}
