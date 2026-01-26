package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.model.DestinationType;
import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
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
            long size, UUID bankAccountId) {
        log.info("Tentando criar job de ingestão: {}, bankAccount: {}", profileId, bankAccountId);

        if (size <= 0) {
            throw new DomainException("Arquivo inválido.");
        }

        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new DomainException("Tipo de arquivo inválido. Somente arquivos .csv são permitidos.");
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
}
