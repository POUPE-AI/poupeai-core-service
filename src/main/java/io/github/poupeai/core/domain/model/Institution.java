package io.github.poupeai.core.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institution {
    private Long id;
    private String name;
    private String mainColorHex;
    private String logoName;
    private InstitutionType type;
}
