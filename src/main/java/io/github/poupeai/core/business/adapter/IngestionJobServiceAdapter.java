package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.audit.Log;
import io.github.poupeai.core.domain.model.DestinationType;
import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.model.JobStatus;
import io.github.poupeai.core.domain.port.business.IngestionJobServicePort;
import io.github.poupeai.core.domain.port.messaging.IngestionJobProducerPort;
import io.github.poupeai.core.domain.port.output.StoragePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.IngestionJobRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.ProfileRepositoryPort;
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

    private final ProfileRepositoryPort profileRepository;
    private final BankAccountRepositoryPort bankAccountRepository;

    @Override
    @Transactional
    public IngestionJob createIngestionJob(UUID profileId, InputStream fileContent, String fileName, String contentType,
                                           long size, UUID bankAccountId,
                                           UUID fallbackIncomeCategoryId,
                                           UUID fallbackExpenseCategoryId) {
        if (size <= 0) {
            throw new DomainException("Arquivo inválido.");
        }

        if (fileName == null || !fileName.toLowerCase().endsWith(".ofx")) {
            throw new DomainException("Tipo de arquivo inválido. Somente arquivos .ofx são permitidos.");
        }

        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + profileId));

        var bankAccount = bankAccountRepository.findByIdAndProfileId(bankAccountId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada ou não pertence ao perfil: " + bankAccountId));

        String fileKey = String.format("statements/%s/%s-%s", profileId, UUID.randomUUID(), fileName);

        try {
            storagePort.upload(fileKey, fileContent, contentType, size,
                    Map.of("profileId", profileId.toString(), "type", "bank-statement"));
        } catch (Exception e) {
            Log.error(log, "INGESTION_FILE_UPLOAD_FAIL", "Erro ao enviar arquivo para o MinIO", e);
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

        payload.put("profile", Map.of(
                "id", profile.getUserId(),
                "name", profile.getFirstName(),
                "email", profile.getEmail()
        ));

        payload.put("bank_account", Map.of(
                "id", bankAccount.getId(),
                "name", bankAccount.getName()
        ));

        payload.put("fallback_income_category_id", fallbackIncomeCategoryId);
        payload.put("fallback_expense_category_id", fallbackExpenseCategoryId);

        PoupeAiEvent<Map<String, Object>> event = PoupeAiEvent.<Map<String, Object>>builder()
                .messageId(UUID.randomUUID())
                .timestamp(java.time.OffsetDateTime.now())
                .triggerType("USER_ACTION")
                .eventType("INGESTION_JOB_CREATED")
                .payload(payload)
                .build();

        messagePublisher.publish(event, "ingestion.job");

        Log.event(log, "INGESTION_JOB_CREATED", "Job de ingestão criado. ID: {}", savedJob.getId());

        return savedJob;
    }

    @Override
    public List<IngestionJob> findAllByProfileId(UUID profileId) {
        return ingestionJobRepository.findAllByProfileId(profileId);
    }

    @Override
    @Transactional
    public void updateJobStatus(UUID jobId, JobStatus status, String summary, String errorDetails) {
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

        Log.event(log, "INGESTION_JOB_UPDATED", "Status do job {} atualizado para {}", jobId, status);
    }
}
