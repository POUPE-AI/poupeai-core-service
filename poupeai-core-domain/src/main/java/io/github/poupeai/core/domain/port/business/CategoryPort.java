package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryPort {
    Category create(Category category);
    Category update(Category category);
    Category findById(UUID id);
    List<Category> findAllByProfileId(UUID profileId);
    void delete(UUID id);
}
