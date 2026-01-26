package io.github.poupeai.core.web.dto.ingestion;

import io.github.poupeai.core.domain.model.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class IngestionJobResponse {
    private UUID id;
    private JobStatus status;
    private String fileKeyMinio;
    private UUID destinationEntityId;
    private OffsetDateTime createdAt;
}
