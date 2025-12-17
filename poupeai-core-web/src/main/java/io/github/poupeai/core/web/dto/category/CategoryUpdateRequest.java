package io.github.poupeai.core.web.dto.category;

import io.github.poupeai.core.domain.model.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    private String name;
    private String colorHex;
    private String iconName;
    private CategoryType type;
}

