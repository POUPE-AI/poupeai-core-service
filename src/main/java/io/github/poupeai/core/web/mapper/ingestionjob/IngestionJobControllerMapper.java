package io.github.poupeai.core.web.mapper.ingestionjob;

import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.web.dto.ingestion.IngestionJobResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngestionJobControllerMapper {
    IngestionJobResponse toResponse(IngestionJob job);

    List<IngestionJobResponse> toResponseList(List<IngestionJob> jobs);
}
