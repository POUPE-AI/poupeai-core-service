package io.github.poupeai.core.web.controller.category;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.port.business.CategoryServicePort;
import io.github.poupeai.core.web.dto.category.InternalCategoryResponse;
import io.github.poupeai.core.web.mapper.category.CategoryControllerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/internal/categories")
@RequiredArgsConstructor
@Tag(name = "Internal Categories", description = "Endpoints internos para consulta de categorias (System-to-System)")
public class InternalCategoryController {
    private final CategoryServicePort categoryServicePort;
    private final CategoryControllerMapper categoryMapper;

    @GetMapping
    @Operation(summary = "Listar categorias por profile_id", description = "Retorna lista simplificada de categorias filtrada pelo ID do perfil.")
    public ResponseEntity<List<InternalCategoryResponse>> listByProfileId(
            @RequestParam("profileId") UUID profileId) {

        List<Category> categories = categoryServicePort.findAllByProfileId(profileId);
        return ResponseEntity.ok(categoryMapper.toInternalResponseList(categories));
    }
}