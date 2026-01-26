package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.domain.port.persistence.IngestionJobRepositoryPort;
import io.github.poupeai.core.persistence.mapper.IngestionJobEntityMapper;
import io.github.poupeai.core.persistence.repository.IngestionJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IngestionJobRepositoryAdapter implements IngestionJobRepositoryPort {

    private final IngestionJobRepository jpaRepository;
    private final IngestionJobEntityMapper mapper;

    @Override
    public IngestionJob save(IngestionJob ingestionJob) {
        var entity = mapper.toEntity(ingestionJob);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<IngestionJob> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<IngestionJob> findAllByProfileId(UUID profileId) {
        var entities = jpaRepository.findAllByProfileIdOrderByCreatedAtDesc(profileId);
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }
}
