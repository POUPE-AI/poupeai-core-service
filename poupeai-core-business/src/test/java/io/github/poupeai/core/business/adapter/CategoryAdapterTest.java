package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryAdapterTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private CategoryAdapter categoryAdapter;

    @Test
    @DisplayName("Should create category successfully when name is available")
    void shouldCreateCategorySuccessfully() {
        Category category = Category.builder()
                .profileId(UUID.randomUUID())
                .name("Food")
                .build();

        when(categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), null))
                .thenReturn(false);
        when(categoryRepositoryPort.create(category)).thenReturn(category);

        Category result = categoryAdapter.create(category);

        assertNotNull(result);
        assertEquals(category.getName(), result.getName());
        verify(categoryRepositoryPort).create(category);
    }

    @Test
    @DisplayName("Should throw DomainException when creating category with duplicate name")
    void shouldThrowExceptionWhenCreatingDuplicateName() {
        Category category = Category.builder()
                .profileId(UUID.randomUUID())
                .name("Food")
                .build();

        when(categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), null))
                .thenReturn(true);

        assertThrows(DomainException.class, () -> categoryAdapter.create(category));
        verify(categoryRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should update category successfully when name is available")
    void shouldUpdateCategorySuccessfully() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder()
                .id(id)
                .profileId(UUID.randomUUID())
                .name("Food Updated")
                .build();

        when(categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), id))
                .thenReturn(false);
        when(categoryRepositoryPort.update(category)).thenReturn(category);

        Category result = categoryAdapter.update(category);

        assertNotNull(result);
        assertEquals("Food Updated", result.getName());
        verify(categoryRepositoryPort).update(category);
    }

    @Test
    @DisplayName("Should throw DomainException when updating category with duplicate name")
    void shouldThrowExceptionWhenUpdatingDuplicateName() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder()
                .id(id)
                .profileId(UUID.randomUUID())
                .name("Food")
                .build();

        when(categoryRepositoryPort.isNameTaken(category.getName(), category.getProfileId(), id))
                .thenReturn(true);

        assertThrows(DomainException.class, () -> categoryAdapter.update(category));
        verify(categoryRepositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Should find category by id successfully")
    void shouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).build();

        when(categoryRepositoryPort.findById(id)).thenReturn(Optional.of(category));

        Category result = categoryAdapter.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("Should throw DomainException when category not found by id")
    void shouldThrowExceptionWhenCategoryNotFound() {
        UUID id = UUID.randomUUID();
        when(categoryRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> categoryAdapter.findById(id));
    }

    @Test
    @DisplayName("Should find all categories by profile id")
    void shouldFindAllByProfileId() {
        UUID profileId = UUID.randomUUID();
        List<Category> categories = List.of(new Category(), new Category());

        when(categoryRepositoryPort.findAllByProfileId(profileId)).thenReturn(categories);

        List<Category> result = categoryAdapter.findAllByProfileId(profileId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should delete category by id")
    void shouldDeleteCategory() {
        UUID id = UUID.randomUUID();

        categoryAdapter.delete(id);

        verify(categoryRepositoryPort).delete(id);
    }
}

