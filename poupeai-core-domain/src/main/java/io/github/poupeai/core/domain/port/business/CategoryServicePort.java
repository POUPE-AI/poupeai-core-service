package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryFilter;
import io.github.poupeai.core.domain.model.PageDomain;

import java.util.List;
import java.util.UUID;

public interface CategoryServicePort {
    Category create(Category category);
    Category update(Category category, UUID profileId);
    Category findByIdAndProfileId(UUID id, UUID profileId);
    List<Category> findAllByProfileId(UUID profileId);
    void delete(UUID id, UUID profileId);
    PageDomain<Category> search(UUID profileId, CategoryFilter filter);
}
