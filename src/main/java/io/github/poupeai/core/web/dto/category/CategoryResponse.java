package io.github.poupeai.core.web.dto.category;

import java.time.OffsetDateTime;

import io.github.poupeai.core.domain.model.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private String colorHex;
    private String iconName;
    private CategoryType type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}