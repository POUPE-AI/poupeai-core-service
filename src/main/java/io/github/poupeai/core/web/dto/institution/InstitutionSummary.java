package io.github.poupeai.core.web.dto.institution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionSummary {
    private Long id;
    private String name;
    private String mainColorHex;
}
