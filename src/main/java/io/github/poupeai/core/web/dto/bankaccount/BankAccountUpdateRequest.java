package io.github.poupeai.core.web.dto.bankaccount;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountUpdateRequest {
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
    private String name;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    private String description;

    private Boolean isDefault;

    private Long institutionId;
}
