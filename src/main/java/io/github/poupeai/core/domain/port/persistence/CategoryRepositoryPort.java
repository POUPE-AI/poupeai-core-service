package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryFilter;
import io.github.poupeai.core.domain.model.PageDomain;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface CategoryRepositoryPort {
    Category create(Category category);
    Category update(Category category, UUID profileId);
    Optional<Category> findByIdAndProfileId(UUID id, UUID profileId);
    List<Category> findAllByProfileId(UUID profileId);
    void delete(UUID id);
    boolean isNameTaken(String name, UUID userId, UUID excludeId);
    boolean existsByIdAndProfileId(UUID id, UUID profileId);
    PageDomain<Category> search(UUID profileId, CategoryFilter filter);
}
