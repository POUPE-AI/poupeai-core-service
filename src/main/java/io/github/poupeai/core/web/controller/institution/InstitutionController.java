package io.github.poupeai.core.web.controller.institution;

import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.domain.port.business.InstitutionServicePort;
import io.github.poupeai.core.web.dto.institution.InstitutionResponse;
import io.github.poupeai.core.web.mapper.institution.InstitutionControllerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
@Tag(name = "Instituições", description = "Listagem de Instituições Financeiras")
public class InstitutionController {
    private final InstitutionServicePort institutionServicePort;
    private final InstitutionControllerMapper institutionMapper;

    @GetMapping
    @Operation(
        summary = "Listar instituições financeiras",
        description = "Retorna todas as instituições financeiras disponíveis",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<List<InstitutionResponse>> getInstitutions() {
        List<Institution> institutions = institutionServicePort.findAll();
        return ResponseEntity.ok(institutionMapper.toResponseList(institutions));
    }
}
