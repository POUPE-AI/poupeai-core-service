package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.Category;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface CategoryRepositoryPort {
    Category create(Category category);
    Category update(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAllByProfileId(UUID profileId);
    void delete(UUID id);
    boolean isNameTaken(String name, UUID userId, UUID excludeId);
}
