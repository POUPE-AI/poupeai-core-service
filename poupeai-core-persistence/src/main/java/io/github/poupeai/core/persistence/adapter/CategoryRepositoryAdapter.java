package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import io.github.poupeai.core.persistence.mapper.CategoryEntityMapper;
import io.github.poupeai.core.persistence.repository.CategoryRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
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
        
        var savedEntity = categoryRepository.save(entity);
        return categoryMapper.toDomain(savedEntity);
    }

    @Override
    public Category update(Category category, UUID profileId) {
        var existingCategory = categoryRepository.findByIdAndProfile_UserId(category.getId(), profileId)
                                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
        
        var profile = profileRepository.getReferenceById(category.getProfileId());
        existingCategory.setProfile(profile);
        existingCategory.setName(category.getName());
        existingCategory.setColorHex(category.getColorHex());
        existingCategory.setIconName(category.getIconName());
        existingCategory.setType(category.getType());

        var savedEntity = categoryRepository.save(existingCategory);
        return categoryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findByIdAndProfileId(UUID id, UUID profileId) {
        return categoryRepository.findByIdAndProfile_UserId(id, profileId).map(categoryMapper::toDomain);
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

    @Override
    public boolean existsByIdAndProfileId(UUID id, UUID profileId) {
        return categoryRepository.existsByIdAndProfile_UserId(id, profileId);
    }
}
