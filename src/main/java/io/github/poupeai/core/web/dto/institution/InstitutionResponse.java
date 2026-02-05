package io.github.poupeai.core.web.dto.institution;

import io.github.poupeai.core.domain.model.InstitutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionResponse {
    private Long id;
    private String name;
    private String mainColorHex;
    private String logoName;
    private InstitutionType type;
}
