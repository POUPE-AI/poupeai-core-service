package io.github.poupeai.core.web.dto.ingestion;

import io.github.poupeai.core.domain.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIngestionJobRequest {
    private JobStatus status;
    private String summary;
    private String errorDetails;
}
