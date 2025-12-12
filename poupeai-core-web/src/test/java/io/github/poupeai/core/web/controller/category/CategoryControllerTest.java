package io.github.poupeai.core.web.controller.category;

import io.github.poupeai.core.domain.exception.ForbiddenActionException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.port.business.CategoryPort;
import io.github.poupeai.core.web.dto.category.CategoryRequest;
import io.github.poupeai.core.web.dto.category.CategoryResponse;
import io.github.poupeai.core.web.mapper.category.CategoryControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryPort categoryPort;

    @Mock
    private CategoryControllerMapper categoryMapper;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    @DisplayName("Should get categories successfully")
    void shouldGetCategoriesSuccessfully() {
        String userId = UUID.randomUUID().toString();
        List<Category> categories = List.of(new Category());
        List<CategoryResponse> responses = List.of(new CategoryResponse());

        when(categoryPort.findAllByProfileId(UUID.fromString(userId))).thenReturn(categories);
        when(categoryMapper.toResponseList(categories)).thenReturn(responses);

        ResponseEntity<List<CategoryResponse>> result = categoryController.getCategories(userId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
    }

    @Test
    @DisplayName("Should get category by id successfully when user is owner")
    void shouldGetCategoryByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).profileId(userId).build();
        CategoryResponse response = new CategoryResponse();

        when(categoryPort.findById(categoryId)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        ResponseEntity<CategoryResponse> result = categoryController.getCategoryById(userId.toString(), categoryId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should throw ForbiddenActionException when getting category of another user")
    void shouldThrowExceptionWhenGettingCategoryOfAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).profileId(otherUserId).build();

        when(categoryPort.findById(categoryId)).thenReturn(category);

        assertThrows(ForbiddenActionException.class, 
            () -> categoryController.getCategoryById(userId.toString(), categoryId));
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() {
        String userId = UUID.randomUUID().toString();
        CategoryRequest request = new CategoryRequest();
        Category category = new Category();
        Category savedCategory = new Category();
        CategoryResponse response = new CategoryResponse();

        when(categoryMapper.toDomain(eq(request), any(UUID.class))).thenReturn(category);
        when(categoryPort.create(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(response);

        ResponseEntity<CategoryResponse> result = categoryController.createCategory(userId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(categoryPort).create(category);
    }

    @Test
    @DisplayName("Should update category successfully when user is owner")
    void shouldUpdateCategorySuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        CategoryRequest request = new CategoryRequest();
        Category existingCategory = Category.builder().id(categoryId).profileId(userId).build();
        Category updatedCategory = new Category();
        CategoryResponse response = new CategoryResponse();

        when(categoryPort.findById(categoryId)).thenReturn(existingCategory);
        when(categoryMapper.toDomain(eq(request), eq(userId))).thenReturn(updatedCategory);
        when(categoryPort.update(updatedCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(updatedCategory)).thenReturn(response);

        ResponseEntity<CategoryResponse> result = categoryController.updateCategory(userId.toString(), categoryId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(updatedCategory).setId(categoryId);
    }

    @Test
    @DisplayName("Should throw ForbiddenActionException when updating category of another user")
    void shouldThrowExceptionWhenUpdatingCategoryOfAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        CategoryRequest request = new CategoryRequest();
        Category existingCategory = Category.builder().id(categoryId).profileId(otherUserId).build();

        when(categoryPort.findById(categoryId)).thenReturn(existingCategory);

        assertThrows(ForbiddenActionException.class, 
            () -> categoryController.updateCategory(userId.toString(), categoryId, request));
        
        verify(categoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Should delete category successfully when user is owner")
    void shouldDeleteCategorySuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).profileId(userId).build();

        when(categoryPort.findById(categoryId)).thenReturn(category);

        ResponseEntity<Void> result = categoryController.deleteCategory(userId.toString(), categoryId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(categoryPort).delete(categoryId);
    }

    @Test
    @DisplayName("Should throw ForbiddenActionException when deleting category of another user")
    void shouldThrowExceptionWhenDeletingCategoryOfAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).profileId(otherUserId).build();

        when(categoryPort.findById(categoryId)).thenReturn(category);

        assertThrows(ForbiddenActionException.class, 
            () -> categoryController.deleteCategory(userId.toString(), categoryId));
        
        verify(categoryPort, never()).delete(any());
    }
}

