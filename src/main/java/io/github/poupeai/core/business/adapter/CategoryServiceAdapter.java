package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.audit.Log;
import io.github.poupeai.core.domain.exception.ResourceAlreadyExistsException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;

import io.github.poupeai.core.domain.model.CategoryFilter;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.port.business.CategoryServicePort;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceAdapter implements CategoryServicePort {
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public Category create(Category category) {
        if (categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), null)) {
            log.warn("Tentativa de criar categoria duplicada: {}", category.getName());
            throw new ResourceAlreadyExistsException("Categoria com este nome já existe");
        }
        Category saved = categoryRepositoryPort.create(category);

        Log.event(log, "CATEGORY_CREATED", "Categoria criada. ID: {}, Nome: {}", saved.getId(), saved.getName());

        return saved;
    }

    @Override
    @Transactional
    public Category update(Category category, UUID profileId) {
        if (categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), category.getId())) {
            log.warn("Tentativa de atualizar para nome de categoria duplicada: {}", category.getName());
            throw new ResourceAlreadyExistsException("Categoria com este nome já existe");
        }
        Category updated = categoryRepositoryPort.update(category, profileId);

        Log.event(log, "CATEGORY_UPDATED", "Categoria atualizada. ID: {}", updated.getId());

        return updated;
    }

    @Override
    public Category findByIdAndProfileId(UUID id, UUID profileId) {
        return categoryRepositoryPort.findByIdAndProfileId(id, profileId)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    }

    @Override
    public List<Category> findAllByProfileId(UUID profileId) {
        return categoryRepositoryPort.findAllByProfileId(profileId);
    }

    @Override
    public void delete(UUID id, UUID profileId) {
        if (!categoryRepositoryPort.existsByIdAndProfileId(id, profileId)) {
            throw new ResourceNotFoundException("Categoria não encontrada.");
        }
        categoryRepositoryPort.delete(id);

        Log.event(log, "CATEGORY_DELETED", "Categoria excluída. ID: {}", id);
    }

    @Override
    public PageDomain<Category> search(UUID profileId, CategoryFilter filter) {
        if (filter.getSortBy() == null || filter.getSortBy().isEmpty()) {
            filter.setSortBy("name");
        }
        return categoryRepositoryPort.search(profileId, filter);
    }
}
