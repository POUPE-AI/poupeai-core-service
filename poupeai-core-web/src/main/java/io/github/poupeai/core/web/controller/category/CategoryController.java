package io.github.poupeai.core.web.controller.category;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.port.business.CategoryServicePort;
import io.github.poupeai.core.web.dto.category.CategoryResponse;
import io.github.poupeai.core.web.mapper.category.CategoryControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.poupeai.core.web.dto.category.CategoryUpdateRequest;
import jakarta.validation.Valid;

import java.util.UUID;
import java.util.List;
import io.github.poupeai.core.web.dto.category.CategoryRequest;


@RestController
@RequestMapping("/api/v1/categories/")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Gerenciamento de Categorias")
public class CategoryController {
    private final CategoryServicePort categoryServicePort;
    private final CategoryControllerMapper categoryMapper;

    @GetMapping
    @Operation(
        summary = "Obter minhas categorias",
        description = "Retorna todas as categorias do usuário",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<List<CategoryResponse>> getCategories(
        @Parameter(hidden = true) @CurrentUserId String userId) {

        List<Category> categories = categoryServicePort.findAllByProfileId(UUID.fromString(userId));
        return ResponseEntity.ok(categoryMapper.toResponseList(categories));
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Obter categoria por ID",
        description = "Retorna detalhes de uma categoria específica",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<CategoryResponse> getCategoryById(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        Category category = categoryServicePort.findByIdAndProfileId(id, UUID.fromString(userId));
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @PostMapping
    @Operation(
        summary = "Criar categoria",
        description = "Cria uma nova categoria",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<CategoryResponse> createCategory(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @RequestBody @Valid CategoryRequest request) {

        Category category = categoryMapper.toDomain(request, UUID.fromString(userId));
        Category savedCategory = categoryServicePort.create(category);

        return ResponseEntity.ok(categoryMapper.toResponse(savedCategory));
    }

    @PatchMapping("{id}")
    @Operation(
        summary = "Atualizar categoria",
        description = "Atualiza os dados de uma categoria existente",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<CategoryResponse> updateCategory(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id, @RequestBody @Valid CategoryUpdateRequest request) {

        Category category = categoryServicePort.findByIdAndProfileId(id, UUID.fromString(userId));
        categoryMapper.updateDomainFromDto(request, category);
        Category updatedCategory = categoryServicePort.update(category, UUID.fromString(userId));

        return ResponseEntity.ok(categoryMapper.toResponse(updatedCategory));
    }

    @DeleteMapping("{id}")
    @Operation(
        summary = "Deletar categoria",
        description = "Deleta uma categoria específica",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<Void> deleteCategory(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        categoryServicePort.delete(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
