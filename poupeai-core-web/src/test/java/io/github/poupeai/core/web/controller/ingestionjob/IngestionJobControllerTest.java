package io.github.poupeai.core.web.controller.ingestionjob;

import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.model.JobStatus;
import io.github.poupeai.core.domain.port.business.IngestionJobServicePort;
import io.github.poupeai.core.web.dto.ingestion.IngestionJobResponse;
import io.github.poupeai.core.web.mapper.ingestionjob.IngestionJobControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionJobControllerTest {

    @Mock
    private IngestionJobServicePort ingestionJobService;

    @Mock
    private IngestionJobControllerMapper mapper;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private IngestionJobController controller;

    @Test
    @DisplayName("Should list ingestion jobs for user")
    void shouldListIngestionJobsForUser() {
        UUID userId = UUID.randomUUID();
        IngestionJob job = IngestionJob.builder().id(UUID.randomUUID()).profileId(userId).build();
        List<IngestionJob> jobs = List.of(job);

        IngestionJobResponse response = IngestionJobResponse.builder().id(job.getId()).build();
        List<IngestionJobResponse> responses = List.of(response);

        when(ingestionJobService.findAllByProfileId(userId)).thenReturn(jobs);
        when(mapper.toResponseList(jobs)).thenReturn(responses);

        ResponseEntity<List<IngestionJobResponse>> result = controller.getIngestionJobs(userId.toString());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(job.getId(), result.getBody().get(0).getId());
    }

    @Test
    @DisplayName("Should import bank statement and return ingestion job")
    void shouldImportBankStatement() throws IOException {
        UUID userId = UUID.randomUUID();
        UUID bankAccountId = UUID.randomUUID();

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("a,b,c".getBytes()));
        when(file.getOriginalFilename()).thenReturn("statement.csv");
        when(file.getContentType()).thenReturn("text/csv");
        when(file.getSize()).thenReturn(5L);

        IngestionJob job = IngestionJob.builder()
                .id(UUID.randomUUID())
                .profileId(userId)
                .status(JobStatus.PENDING)
                .build();

        IngestionJobResponse response = IngestionJobResponse.builder().id(job.getId()).status(JobStatus.PENDING).build();

        when(ingestionJobService.createIngestionJob(eq(userId), any(), eq("statement.csv"), eq("text/csv"), eq(5L), eq(bankAccountId)))
                .thenReturn(job);
        when(mapper.toResponse(job)).thenReturn(response);

        ResponseEntity<IngestionJobResponse> result = controller.importBankStatement(userId.toString(), file, bankAccountId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(job.getId(), result.getBody().getId());

        verify(ingestionJobService).createIngestionJob(eq(userId), any(), eq("statement.csv"), eq("text/csv"), eq(5L), eq(bankAccountId));
        verify(mapper).toResponse(job);
    }
}
