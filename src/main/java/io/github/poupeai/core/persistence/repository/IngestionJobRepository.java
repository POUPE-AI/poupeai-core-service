package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.IngestionJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IngestionJobRepository extends JpaRepository<IngestionJobEntity, UUID> {
    List<IngestionJobEntity> findAllByProfileIdOrderByCreatedAtDesc(UUID profileId);
}
