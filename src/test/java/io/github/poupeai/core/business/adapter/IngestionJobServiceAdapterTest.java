package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.model.JobStatus;
import io.github.poupeai.core.domain.port.messaging.IngestionJobProducerPort;
import io.github.poupeai.core.domain.port.output.StoragePort;
import io.github.poupeai.core.domain.port.persistence.IngestionJobRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionJobServiceAdapterTest {

    @Mock
    private IngestionJobRepositoryPort ingestionJobRepository;

    @Mock
    private IngestionJobProducerPort messagePublisher;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private IngestionJobServiceAdapter service;

    @Test
    @DisplayName("Should throw DomainException when size is invalid")
    void shouldThrowDomainExceptionWhenSizeInvalid() {
        UUID profileId = UUID.randomUUID();

        DomainException ex = assertThrows(DomainException.class, () -> service.createIngestionJob(
                profileId,
                new ByteArrayInputStream(new byte[0]),
                "statement.csv",
                "text/csv",
                0,
                UUID.randomUUID(),
                null,
                null));

        assertEquals("Arquivo inválido.", ex.getMessage());
        verifyNoInteractions(storagePort, ingestionJobRepository, messagePublisher);
    }

    @Test
    @DisplayName("Should throw DomainException when file extension is invalid")
    void shouldThrowDomainExceptionWhenExtensionInvalid() {
        UUID profileId = UUID.randomUUID();

        DomainException ex = assertThrows(DomainException.class, () -> service.createIngestionJob(
                profileId,
                new ByteArrayInputStream("content".getBytes()),
                "statement.pdf",
                "application/pdf",
                7,
                UUID.randomUUID(),
                null,
                null));

        assertEquals("Tipo de arquivo inválido. Somente arquivos .ofx são permitidos.", ex.getMessage());
        verifyNoInteractions(storagePort, ingestionJobRepository, messagePublisher);
    }

    @Test
    @DisplayName("Should create ingestion job, upload to storage and publish event with fallbacks")
    void shouldCreateIngestionJobUploadAndPublish() {
        UUID profileId = UUID.randomUUID();
        UUID bankAccountId = UUID.randomUUID();
        UUID incomeFallbackId = UUID.randomUUID();
        UUID expenseFallbackId = UUID.randomUUID();

        InputStream fileContent = new ByteArrayInputStream("a,b,c".getBytes());

        IngestionJob savedJob = IngestionJob.builder()
                .id(UUID.randomUUID())
                .profileId(profileId)
                .status(JobStatus.PENDING)
                .fileKeyMinio("ignored")
                .build();

        when(ingestionJobRepository.save(any(IngestionJob.class))).thenAnswer(inv -> {
            IngestionJob arg = inv.getArgument(0);
            arg.setId(savedJob.getId());
            return arg;
        });

        IngestionJob result = service.createIngestionJob(
                profileId,
                fileContent,
                "statement.ofx",
                "application/x-ofx",
                5,
                bankAccountId,
                incomeFallbackId,
                expenseFallbackId);

        assertNotNull(result);
        assertEquals(savedJob.getId(), result.getId());
        assertEquals(JobStatus.PENDING, result.getStatus());
        assertEquals(bankAccountId, result.getDestinationEntityId());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storagePort).upload(
                keyCaptor.capture(),
                same(fileContent),
                eq("application/x-ofx"),
                eq(5L),
                eq(Map.of("profileId", profileId.toString(), "type", "bank-statement")));

        String key = keyCaptor.getValue();
        assertNotNull(key);
        assertTrue(key.startsWith("statements/" + profileId + "/"));
        assertTrue(key.endsWith("-statement.ofx"));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagePublisher).publish(eventCaptor.capture(), eq("ingestion.job"));

        Object published = eventCaptor.getValue();
        assertTrue(published instanceof PoupeAiEvent);

        @SuppressWarnings("unchecked")
        PoupeAiEvent<Map<String, Object>> event = (PoupeAiEvent<Map<String, Object>>) published;
        assertEquals("INGESTION_JOB_CREATED", event.getEventType());

        Map<String, Object> payload = event.getPayload();
        assertNotNull(payload);
        assertEquals(savedJob.getId(), payload.get("job_id"));
        assertEquals(profileId, payload.get("profile_id"));
        assertEquals(bankAccountId, payload.get("bank_account_id"));
        assertEquals(key, payload.get("file_key"));
        assertEquals(incomeFallbackId, payload.get("fallback_income_category_id"));
        assertEquals(expenseFallbackId, payload.get("fallback_expense_category_id"));

        verify(ingestionJobRepository).save(any(IngestionJob.class));
    }

    @Test
    @DisplayName("Should list ingestion jobs by profile id")
    void shouldListIngestionJobsByProfileId() {
        UUID profileId = UUID.randomUUID();
        List<IngestionJob> jobs = List.of(IngestionJob.builder().id(UUID.randomUUID()).profileId(profileId).build());

        when(ingestionJobRepository.findAllByProfileId(profileId)).thenReturn(jobs);

        List<IngestionJob> result = service.findAllByProfileId(profileId);

        assertEquals(1, result.size());
        verify(ingestionJobRepository).findAllByProfileId(profileId);
    }

    @Test
    @DisplayName("Should update job status successfully")
    void shouldUpdateJobStatus() {
        UUID jobId = UUID.randomUUID();
        IngestionJob existingJob = IngestionJob.builder()
                .id(jobId)
                .status(JobStatus.PENDING)
                .build();

        when(ingestionJobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));

        service.updateJobStatus(jobId, JobStatus.COMPLETED, "Resumo", null);

        assertEquals(JobStatus.COMPLETED, existingJob.getStatus());
        assertEquals("Resumo", existingJob.getSummary());
        verify(ingestionJobRepository).save(existingJob);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent job")
    void shouldThrowExceptionWhenUpdatingNonExistentJob() {
        UUID jobId = UUID.randomUUID();
        when(ingestionJobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.updateJobStatus(jobId, JobStatus.FAILED, null, "Error")
        );

        verify(ingestionJobRepository, never()).save(any());
    }
}
