package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.model.Category;

import io.github.poupeai.core.domain.port.business.CategoryPort;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryPort {
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public Category create(Category category) {
        if (categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), null)) {
            throw new DomainException("Categoria com este nome já existe");
        }
        return categoryRepositoryPort.create(category);
    }

    @Override
    @Transactional
    public Category update(Category category) {
        if (categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), category.getId())) {
            throw new DomainException("Categoria com este nome já existe");
        }
        return categoryRepositoryPort.update(category);
    }

    @Override
    public Category findById(UUID id) {
        return categoryRepositoryPort.findById(id)
            .orElseThrow(() -> new DomainException("Categoria não encontrada."));
    }

    @Override
    public List<Category> findAllByProfileId(UUID profileId) {
        return categoryRepositoryPort.findAllByProfileId(profileId);
    }

    @Override
    public void delete(UUID id) {
        categoryRepositoryPort.delete(id);
    }
}
