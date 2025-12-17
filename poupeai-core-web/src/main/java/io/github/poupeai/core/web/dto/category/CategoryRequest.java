package io.github.poupeai.core.web.dto.category;

import io.github.poupeai.core.domain.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank
    private String name;
    private String colorHex;
    private String iconName;
    @NotNull
    private CategoryType type;
}