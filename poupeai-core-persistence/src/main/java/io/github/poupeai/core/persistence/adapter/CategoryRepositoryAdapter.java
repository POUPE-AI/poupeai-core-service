package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import io.github.poupeai.core.persistence.mapper.CategoryEntityMapper;
import io.github.poupeai.core.persistence.repository.CategoryRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import io.github.poupeai.core.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {
    private final CategoryRepository categoryRepository;
    private final CategoryEntityMapper categoryMapper;
    private final ProfileRepository profileRepository;

    @Override
    public Category create(Category category) {
        var entity = categoryMapper.toEntity(category);
        
        var profile = profileRepository.getReferenceById(category.getProfileId());
        entity.setProfile(profile);

        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        
        var savedEntity = categoryRepository.save(entity);
        return categoryMapper.toDomain(savedEntity);
    }

    @Override
    public Category update(Category category) {
        var existingCategory = categoryRepository.findById(category.getId())
                                .orElseThrow(() -> new DomainException("Categoria não encontrada."));
        
        var profile = profileRepository.getReferenceById(category.getProfileId());
        existingCategory.setProfile(profile);
        existingCategory.setName(category.getName());
        existingCategory.setColorHex(category.getColorHex());
        existingCategory.setIconName(category.getIconName());
        existingCategory.setType(category.getType());
        existingCategory.setUpdatedAt(OffsetDateTime.now());

        var savedEntity = categoryRepository.save(existingCategory);
        return categoryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id).map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAllByProfileId(UUID profileId) {
        var entities = categoryRepository.findAllByProfile_UserId(profileId);
        return categoryMapper.toDomainList(entities);
    }

    @Override
    public void delete(UUID id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean isNameTaken(String name, UUID userId, UUID excludeId) {
        if (excludeId == null) {
            return categoryRepository.existsByNameAndProfile_UserId(name, userId);
        }
        return categoryRepository.existsByNameAndProfile_UserIdAndIdNot(name, userId, excludeId);
    }
}
