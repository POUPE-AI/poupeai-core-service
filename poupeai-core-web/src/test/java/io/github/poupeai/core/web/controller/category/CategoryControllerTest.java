package io.github.poupeai.core.web.controller.category;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryFilter;
import io.github.poupeai.core.domain.model.CategoryType;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.port.business.CategoryServicePort;
import io.github.poupeai.core.web.dto.category.CategoryRequest;
import io.github.poupeai.core.web.dto.category.CategoryResponse;
import io.github.poupeai.core.web.dto.category.CategoryUpdateRequest;
import io.github.poupeai.core.web.dto.common.PageResponse;
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
    private CategoryServicePort categoryServicePort;

    @Mock
    private CategoryControllerMapper categoryMapper;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    @DisplayName("Should get categories successfully")
    void shouldGetCategoriesSuccessfully() {
        String userIdStr = UUID.randomUUID().toString();
        UUID userId = UUID.fromString(userIdStr);

        int page = 0;
        int size = 10;
        String name = "Test";
        CategoryType type = CategoryType.EXPENSE;
        String sortDirection = "DESC";
        String sortBy = "createdAt";

        Category category = new Category();
        CategoryResponse categoryResponse = new CategoryResponse();
        List<Category> categories = List.of(category);
        List<CategoryResponse> responses = List.of(categoryResponse);

        CategoryFilter expectedFilter = CategoryFilter.builder()
                .page(page)
                .size(size)
                .name(name)
                .type(type)
                .sortDirection(sortDirection)
                .sortBy(sortBy)
                .build();

        PageDomain<Category> pageDomain = PageDomain.<Category>builder()
                .content(categories)
                .page(page)
                .size(size)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(categoryServicePort.search(eq(userId), eq(expectedFilter))).thenReturn(pageDomain);
        when(categoryMapper.toResponseList(categories)).thenReturn(responses);

        ResponseEntity<PageResponse<CategoryResponse>> result = categoryController.getCategories(
                userIdStr, page, size, name, type, sortDirection, sortBy
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(responses, result.getBody().getContent());
        assertEquals(1, result.getBody().getTotalElements());
        assertEquals(1, result.getBody().getTotalPages());

        verify(categoryServicePort).search(eq(userId), eq(expectedFilter));
    }

    @Test
    @DisplayName("Should get category by id successfully when user is owner")
    void shouldGetCategoryByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).profileId(userId).build();
        CategoryResponse response = new CategoryResponse();

        when(categoryServicePort.findByIdAndProfileId(categoryId, userId)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        ResponseEntity<CategoryResponse> result = categoryController.getCategoryById(userId.toString(), categoryId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
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
        when(categoryServicePort.create(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(response);

        ResponseEntity<CategoryResponse> result = categoryController.createCategory(userId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(categoryServicePort).create(category);
    }

    @Test
    @DisplayName("Should update category successfully when user is owner")
    void shouldUpdateCategorySuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        Category category = Category.builder().id(categoryId).profileId(userId).build();
        CategoryResponse response = new CategoryResponse();

        when(categoryServicePort.findByIdAndProfileId(categoryId, userId)).thenReturn(category);
        doNothing().when(categoryMapper).updateDomainFromDto(eq(request), eq(category));
        when(categoryServicePort.update(category, userId)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        ResponseEntity<CategoryResponse> result = categoryController.updateCategory(userId.toString(), categoryId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(categoryServicePort).update(category, userId);
    }

    @Test
    @DisplayName("Should delete category successfully when user is owner")
    void shouldDeleteCategorySuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        ResponseEntity<Void> result = categoryController.deleteCategory(userId.toString(), categoryId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(categoryServicePort).delete(categoryId, userId);
    }
}

