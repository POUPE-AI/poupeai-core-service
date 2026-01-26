package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionJob {
    private UUID id;
    private UUID profileId;
    private JobStatus status;
    private String fileKeyMinio;
    private DestinationType destinationEntityType;
    private UUID destinationEntityId;
    private String summary;
    private String errorDetails;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
