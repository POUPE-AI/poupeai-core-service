package io.github.poupeai.core.web.controller.ingestionjob;

import io.github.poupeai.core.domain.port.business.IngestionJobServicePort;
import io.github.poupeai.core.web.dto.ingestion.UpdateIngestionJobRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/internal/ingestion-jobs")
@RequiredArgsConstructor
@Tag(name = "Internal Ingestion Jobs", description = "Endpoints internos para atualização de status de jobs (System-to-System)")
public class InternalIngestionJobController {

    private final IngestionJobServicePort ingestionJobService;

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar status do job", description = "Endpoint usado pelo Ingestion Service para reportar progresso/conclusão.")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateIngestionJobRequest request) {

        log.info("Recebendo atualização de status para Job {}: {}", id, request.getStatus());

        ingestionJobService.updateJobStatus(
                id,
                request.getStatus(),
                request.getSummary(),
                request.getErrorDetails()
        );

        return ResponseEntity.noContent().build();
    }
}
