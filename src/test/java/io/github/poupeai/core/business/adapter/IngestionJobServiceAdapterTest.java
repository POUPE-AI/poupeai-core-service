package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.model.JobStatus;
import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.messaging.IngestionJobProducerPort;
import io.github.poupeai.core.domain.port.output.StoragePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.IngestionJobRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.ProfileRepositoryPort;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionJobServiceAdapterTest {

    @Mock
    private IngestionJobRepositoryPort ingestionJobRepository;

    @Mock
    private IngestionJobProducerPort messagePublisher;

    @Mock
    private StoragePort storagePort;

    @Mock
    private ProfileRepositoryPort profileRepository;

    @Mock
    private BankAccountRepositoryPort bankAccountRepository;

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
    }

    @Test
    @DisplayName("Should create ingestion job, upload to storage and publish enriched payload")
    void shouldCreateIngestionJobUploadAndPublish() {
        UUID profileId = UUID.randomUUID();
        UUID bankAccountId = UUID.randomUUID();
        UUID incomeFallbackId = UUID.randomUUID();
        UUID expenseFallbackId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .userId(profileId)
                .firstName("John Doe")
                .email("john@email.com")
                .build();

        BankAccount bankAccount = BankAccount.builder()
                .id(bankAccountId)
                .profileId(profileId)
                .name("Main Account")
                .build();

        InputStream fileContent = new ByteArrayInputStream("a,b,c".getBytes());

        IngestionJob savedJob = IngestionJob.builder()
                .id(UUID.randomUUID())
                .profileId(profileId)
                .status(JobStatus.PENDING)
                .fileKeyMinio("ignored")
                .build();

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(bankAccountRepository.findByIdAndProfileId(bankAccountId, profileId)).thenReturn(Optional.of(bankAccount));

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

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storagePort).upload(keyCaptor.capture(), any(), any(), anyLong(), anyMap());
        String key = keyCaptor.getValue();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagePublisher).publish(eventCaptor.capture(), eq("ingestion.job"));

        @SuppressWarnings("unchecked")
        PoupeAiEvent<Map<String, Object>> event = (PoupeAiEvent<Map<String, Object>>) eventCaptor.getValue();

        Map<String, Object> payload = event.getPayload();
        assertNotNull(payload);

        @SuppressWarnings("unchecked")
        Map<String, Object> profilePayload = (Map<String, Object>) payload.get("profile");
        assertEquals(profile.getEmail(), profilePayload.get("email"));

        @SuppressWarnings("unchecked")
        Map<String, Object> accountPayload = (Map<String, Object>) payload.get("bank_account");
        assertEquals(bankAccount.getName(), accountPayload.get("name"));

        assertNull(event.getRecipient());
    }

    @Test
    @DisplayName("Should list ingestion jobs by profile id")
    void shouldListIngestionJobsByProfileId() {
        UUID profileId = UUID.randomUUID();
        when(ingestionJobRepository.findAllByProfileId(profileId)).thenReturn(List.of());
        service.findAllByProfileId(profileId);
        verify(ingestionJobRepository).findAllByProfileId(profileId);
    }

    @Test
    @DisplayName("Should update job status successfully")
    void shouldUpdateJobStatus() {
        UUID jobId = UUID.randomUUID();
        IngestionJob job = IngestionJob.builder().id(jobId).build();
        when(ingestionJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        service.updateJobStatus(jobId, JobStatus.COMPLETED, null, null);
        verify(ingestionJobRepository).save(job);
    }
}