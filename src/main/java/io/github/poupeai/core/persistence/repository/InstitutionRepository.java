package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.InstitutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<InstitutionEntity, Long> {
}
