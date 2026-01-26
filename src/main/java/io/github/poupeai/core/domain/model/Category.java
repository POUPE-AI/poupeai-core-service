package io.github.poupeai.core.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private UUID id;
    private UUID profileId;
    private String name;
    private String colorHex;
    private String iconName;
    private CategoryType type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
