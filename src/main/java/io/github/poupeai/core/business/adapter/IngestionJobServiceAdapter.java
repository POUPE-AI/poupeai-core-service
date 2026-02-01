package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.model.DestinationType;
import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.model.JobStatus;
import io.github.poupeai.core.domain.port.business.IngestionJobServicePort;
import io.github.poupeai.core.domain.port.messaging.IngestionJobProducerPort;
import io.github.poupeai.core.domain.port.output.StoragePort;
import io.github.poupeai.core.domain.port.persistence.IngestionJobRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionJobServiceAdapter implements IngestionJobServicePort {

    private final IngestionJobRepositoryPort ingestionJobRepository;
    private final IngestionJobProducerPort messagePublisher;
    private final StoragePort storagePort;

    @Override
    @Transactional
    public IngestionJob createIngestionJob(UUID profileId, InputStream fileContent, String fileName, String contentType,
                                           long size, UUID bankAccountId, UUID fallbackCategoryId) {
        log.info("Tentando criar job de ingestão: {}, bankAccount: {}, fallbackCategory: {}", profileId, bankAccountId, fallbackCategoryId);

        if (size <= 0) {
            throw new DomainException("Arquivo inválido.");
        }

        if (fileName == null || !fileName.toLowerCase().endsWith(".ofx")) {
            throw new DomainException("Tipo de arquivo inválido. Somente arquivos .ofx são permitidos.");
        }

        String fileKey = String.format("statements/%s/%s-%s", profileId, UUID.randomUUID(), fileName);

        try {
            storagePort.upload(fileKey, fileContent, contentType, size,
                    Map.of("profileId", profileId.toString(), "type", "bank-statement"));
        } catch (Exception e) {
            log.error("Erro ao enviar arquivo para o MinIO", e);
            throw new RuntimeException("Falha ao enviar arquivo para o storage", e);
        }

        IngestionJob job = IngestionJob.builder()
                .profileId(profileId)
                .status(JobStatus.PENDING)
                .fileKeyMinio(fileKey)
                .destinationEntityType(DestinationType.BANK_ACCOUNT)
                .destinationEntityId(bankAccountId)
                .build();

        var savedJob = ingestionJobRepository.save(job);

        Map<String, Object> payload = new HashMap<>();
        payload.put("job_id", savedJob.getId());
        payload.put("file_key", fileKey);
        payload.put("profile_id", profileId);
        payload.put("bank_account_id", bankAccountId);
        payload.put("fallback_category_id", fallbackCategoryId);

        PoupeAiEvent<Map<String, Object>> event = PoupeAiEvent.<Map<String, Object>>builder()
                .messageId(UUID.randomUUID())
                .timestamp(java.time.OffsetDateTime.now())
                .triggerType("USER_ACTION")
                .eventType("INGESTION_JOB_CREATED")
                .payload(payload)
                .build();

        messagePublisher.publish(event, "ingestion.job");

        return savedJob;
    }

    @Override
    public List<IngestionJob> findAllByProfileId(UUID profileId) {
        return ingestionJobRepository.findAllByProfileId(profileId);
    }

    @Override
    @Transactional
    public void updateJobStatus(UUID jobId, JobStatus status, String summary, String errorDetails) {
        log.info("Atualizando status do job {} para {}", jobId, status);

        IngestionJob job = ingestionJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job de ingestão não encontrado: " + jobId));

        job.setStatus(status);

        if (summary != null) {
            job.setSummary(summary);
        }

        if (errorDetails != null) {
            job.setErrorDetails(errorDetails);
        }

        ingestionJobRepository.save(job);
    }
}
