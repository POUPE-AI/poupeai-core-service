package io.github.poupeai.core.web.controller.ingestionjob;

import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.port.business.IngestionJobServicePort;
import io.github.poupeai.core.web.dto.ingestion.IngestionJobResponse;
import io.github.poupeai.core.web.mapper.ingestionjob.IngestionJobControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingestion-jobs")
@RequiredArgsConstructor
@Tag(name = "Ingestion Jobs", description = "Gerenciamento de Jobs de Ingestão")
public class IngestionJobController {

    private final IngestionJobServicePort ingestionJobService;
    private final IngestionJobControllerMapper mapper;

    @GetMapping
    @Operation(summary = "Listar jobs de ingestão", description = "Retorna todos os jobs de ingestão do usuário", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<List<IngestionJobResponse>> getIngestionJobs(
            @Parameter(hidden = true) @CurrentUserId String userId) {

        UUID profileId = UUID.fromString(userId);
        List<IngestionJob> jobs = ingestionJobService.findAllByProfileId(profileId);
        return ResponseEntity.ok(mapper.toResponseList(jobs));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar extrato bancário", description = "Realiza o upload de um extrato bancário (CSV) para processamento.", security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<IngestionJobResponse> importBankStatement(
            @Parameter(hidden = true) @CurrentUserId String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("bankAccountId") UUID bankAccountId) throws IOException {

        UUID profileId = UUID.fromString(userId);

        IngestionJob job = ingestionJobService.createIngestionJob(
                profileId,
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                bankAccountId);

        return ResponseEntity.ok(mapper.toResponse(job));
    }
}
