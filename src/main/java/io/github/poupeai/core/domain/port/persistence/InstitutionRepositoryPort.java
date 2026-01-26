package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.Institution;

import java.util.List;

public interface InstitutionRepositoryPort {
    List<Institution> findAll();
    boolean existsById(Long id);
}
