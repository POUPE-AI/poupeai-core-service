package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.model.JobStatus;
import io.github.poupeai.core.domain.port.output.MessagePublisherPort;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionJobServiceAdapterTest {

    @Mock
    private IngestionJobRepositoryPort ingestionJobRepository;

    @Mock
    private MessagePublisherPort messagePublisher;

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
                UUID.randomUUID()));

        assertEquals("Arquivo inválido.", ex.getMessage());
        verifyNoInteractions(storagePort, ingestionJobRepository, messagePublisher);
    }

    @Test
    @DisplayName("Should throw DomainException when file extension is not csv")
    void shouldThrowDomainExceptionWhenExtensionInvalid() {
        UUID profileId = UUID.randomUUID();

        DomainException ex = assertThrows(DomainException.class, () -> service.createIngestionJob(
                profileId,
                new ByteArrayInputStream("content".getBytes()),
                "statement.pdf",
                "application/pdf",
                7,
                UUID.randomUUID()));

        assertEquals("Tipo de arquivo inválido. Somente arquivos .csv são permitidos.", ex.getMessage());
        verifyNoInteractions(storagePort, ingestionJobRepository, messagePublisher);
    }

    @Test
    @DisplayName("Should create ingestion job, upload to storage and publish event")
    void shouldCreateIngestionJobUploadAndPublish() {
        UUID profileId = UUID.randomUUID();
        UUID bankAccountId = UUID.randomUUID();
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
                "statement.csv",
                "text/csv",
                5,
                bankAccountId);

        assertNotNull(result);
        assertEquals(savedJob.getId(), result.getId());
        assertEquals(JobStatus.PENDING, result.getStatus());
        assertEquals(bankAccountId, result.getDestinationEntityId());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storagePort).upload(
                keyCaptor.capture(),
                same(fileContent),
                eq("text/csv"),
                eq(5L),
                eq(Map.of("profileId", profileId.toString(), "type", "bank-statement")));

        String key = keyCaptor.getValue();
        assertNotNull(key);
        assertTrue(key.startsWith("statements/" + profileId + "/"));
        assertTrue(key.endsWith("-statement.csv"));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagePublisher).publish(eventCaptor.capture(), eq("ingestion.job"));

        Object published = eventCaptor.getValue();
        assertTrue(published instanceof PoupeAiEvent);

        @SuppressWarnings("unchecked")
        PoupeAiEvent<Map<String, Object>> event = (PoupeAiEvent<Map<String, Object>>) published;
        assertEquals("INGESTION_JOB_CREATED", event.getEventType());
        assertNotNull(event.getMessageId());
        assertNotNull(event.getTimestamp());

        Map<String, Object> payload = event.getPayload();
        assertNotNull(payload);
        assertEquals(savedJob.getId(), payload.get("job_id"));
        assertEquals(profileId, payload.get("profile_id"));
        assertEquals(bankAccountId, payload.get("bank_account_id"));
        assertEquals(key, payload.get("file_key"));

        verify(ingestionJobRepository).save(any(IngestionJob.class));
        verifyNoMoreInteractions(messagePublisher);
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
}
