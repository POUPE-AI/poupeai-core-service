package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.IngestionJob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngestionJobRepositoryPort {
    IngestionJob save(IngestionJob ingestionJob);

    Optional<IngestionJob> findById(UUID id);

    List<IngestionJob> findAllByProfileId(UUID profileId);
}
