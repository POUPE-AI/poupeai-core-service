package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.persistence.entity.CategoryEntity;
import io.github.poupeai.core.persistence.entity.ProfileEntity;
import io.github.poupeai.core.persistence.mapper.CategoryEntityMapper;
import io.github.poupeai.core.persistence.repository.CategoryRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

    @InjectMocks
    private CategoryRepositoryAdapter adapter;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryEntityMapper categoryMapper;

    @Mock
    private ProfileRepository profileRepository;

    @Test
    @DisplayName("Should persist category and link profile when data is valid")
    void createShouldPersistWhenDataIsValid() {
        UUID profileId = UUID.randomUUID();
        Category domain = new Category();
        domain.setProfileId(profileId);

        CategoryEntity entity = new CategoryEntity();
        ProfileEntity profileProxy = new ProfileEntity();

        when(categoryMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(categoryRepository.save(entity)).thenAnswer(invocation -> {
            CategoryEntity saved = invocation.getArgument(0);
            saved.setCreatedAt(OffsetDateTime.now());
            saved.setUpdatedAt(OffsetDateTime.now());
            return saved;
        });
        when(categoryMapper.toDomain(entity)).thenReturn(domain);

        Category result = adapter.create(domain);

        assertNotNull(result);
        verify(categoryRepository).save(entity);
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update category fields when category exists")
    void updateShouldUpdateFieldsWhenCategoryExists() {
        UUID catId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Category domain = new Category();
        domain.setId(catId);
        domain.setName("Updated Category");

        CategoryEntity existingEntity = new CategoryEntity();
        ProfileEntity profileProxy = new ProfileEntity();

        when(categoryRepository.findByIdAndProfile_UserId(catId, profileId))
                .thenReturn(Optional.of(existingEntity));
        when(profileRepository.getReferenceById(any())).thenReturn(profileProxy);
        when(categoryRepository.save(existingEntity)).thenAnswer(invocation -> {
            CategoryEntity saved = invocation.getArgument(0);
            saved.setUpdatedAt(OffsetDateTime.now());
            return saved;
        });
        when(categoryMapper.toDomain(existingEntity)).thenReturn(domain);

        Category result = adapter.update(domain, profileId);

        assertNotNull(result);
        assertEquals("Updated Category", existingEntity.getName());
        assertNotNull(existingEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist during update")
    void updateShouldThrowNotFoundWhenCategoryDoesNotExist() {
        UUID catId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        Category domain = new Category();
        domain.setId(catId);

        when(categoryRepository.findByIdAndProfile_UserId(catId, profileId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adapter.update(domain, profileId)
        );

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return empty optional when category is not found by ID and Profile ID")
    void findByIdShouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(categoryRepository.findByIdAndProfile_UserId(id, profileId)).thenReturn(Optional.empty());

        Optional<Category> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return list of categories mapped to domain")
    void findAllShouldReturnList() {
        UUID profileId = UUID.randomUUID();
        List<CategoryEntity> entities = List.of(new CategoryEntity());
        List<Category> domains = List.of(new Category());

        when(categoryRepository.findAllByProfile_UserId(profileId)).thenReturn(entities);
        when(categoryMapper.toDomainList(entities)).thenReturn(domains);

        List<Category> result = adapter.findAllByProfileId(profileId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should call delete by ID in repository")
    void deleteShouldCallRepository() {
        UUID id = UUID.randomUUID();
        adapter.delete(id);
        verify(categoryRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should check name availability ignoring exclusion when excludeId is null")
    void isNameTakenShouldCheckAllWhenExcludeIdIsNull() {
        String name = "Groceries";
        UUID userId = UUID.randomUUID();
        when(categoryRepository.existsByNameAndProfile_UserId(name, userId)).thenReturn(true);

        boolean result = adapter.isNameTaken(name, userId, null);

        assertTrue(result);
        verify(categoryRepository).existsByNameAndProfile_UserId(name, userId);
        verify(categoryRepository, never()).existsByNameAndProfile_UserIdAndIdNot(any(), any(), any());
    }

    @Test
    @DisplayName("Should check name availability excluding specific ID when excludeId is provided")
    void isNameTakenShouldCheckExcludingIdWhenExcludeIdIsNotNull() {
        String name = "Groceries";
        UUID userId = UUID.randomUUID();
        UUID excludeId = UUID.randomUUID();
        when(categoryRepository.existsByNameAndProfile_UserIdAndIdNot(name, userId, excludeId)).thenReturn(false);

        boolean result = adapter.isNameTaken(name, userId, excludeId);

        assertFalse(result);
        verify(categoryRepository).existsByNameAndProfile_UserIdAndIdNot(name, userId, excludeId);
        verify(categoryRepository, never()).existsByNameAndProfile_UserId(any(), any());
    }

    @Test
    @DisplayName("Should return true when category exists for the given profile")
    void existsByIdShouldReturnRepositoryResult() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(categoryRepository.existsByIdAndProfile_UserId(id, profileId)).thenReturn(true);

        assertTrue(adapter.existsByIdAndProfileId(id, profileId));
    }
}
