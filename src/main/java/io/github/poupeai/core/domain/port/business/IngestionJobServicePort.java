package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.model.JobStatus;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface IngestionJobServicePort {
    IngestionJob createIngestionJob(UUID profileId, InputStream fileContent, String fileName, String contentType,
            long size, UUID bankAccountId, UUID fallbackCategoryId);

    List<IngestionJob> findAllByProfileId(UUID profileId);

    void updateJobStatus(UUID jobId, JobStatus status, String summary, String errorDetails);
}
