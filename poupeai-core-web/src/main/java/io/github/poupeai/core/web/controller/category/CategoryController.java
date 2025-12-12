package io.github.poupeai.core.web.controller.category;

import io.github.poupeai.core.domain.exception.ForbiddenActionException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.port.business.CategoryPort;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.UUID;
import java.util.List;
import io.github.poupeai.core.web.dto.category.CategoryRequest;


@RestController
@RequestMapping("/api/v1/categories/")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Gerenciamento de Categorias")
public class CategoryController {
    private final CategoryPort categoryPort;
    private final CategoryControllerMapper categoryMapper;

    @GetMapping
    @Operation(
        summary = "Obter minhas categorias",
        description = "Retorna todas as categorias do usuário",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<List<CategoryResponse>> getCategories(
        @Parameter(hidden = true) @CurrentUserId String userId) {
        UUID userUUID = UUID.fromString(userId);

        List<Category> categories = categoryPort.findAllByProfileId(userUUID);

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
        UUID userUUID = UUID.fromString(userId);
        Category category = categoryPort.findById(id);
        
        if (!category.getProfileId().equals(userUUID)) {
            throw new ForbiddenActionException("Você não tem permissão para acessar esta categoria.");
        }
        
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
        UUID userUUID = UUID.fromString(userId);
        Category category = categoryMapper.toDomain(request, userUUID);
        Category savedCategory = categoryPort.create(category);
        return ResponseEntity.ok(categoryMapper.toResponse(savedCategory));
    }

    @PutMapping("{id}")
    @Operation(
        summary = "Atualizar categoria",
        description = "Atualiza os dados de uma categoria existente",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<CategoryResponse> updateCategory(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id, @RequestBody @Valid CategoryRequest request) {
        UUID userUUID = UUID.fromString(userId);
        
        Category existingCategory = categoryPort.findById(id);
        if (!existingCategory.getProfileId().equals(userUUID)) {
            throw new ForbiddenActionException("Você não tem permissão para alterar esta categoria.");
        }
        
        Category category = categoryMapper.toDomain(request, userUUID);
        category.setId(id);
        Category updatedCategory = categoryPort.update(category);
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
        Category category = categoryPort.findById(id);
        if (!category.getProfileId().equals(UUID.fromString(userId))) {
            throw new ForbiddenActionException("Você não tem permissão para deletar esta categoria.");
        }
        categoryPort.delete(id);
        return ResponseEntity.ok().build();
    }
}
