package io.github.poupeai.core.web.mapper.category;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.web.dto.category.CategoryRequest;
import io.github.poupeai.core.web.dto.category.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryControllerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toDomain(CategoryRequest request, UUID profileId);

    CategoryResponse toResponse(Category domain);
    
    List<CategoryResponse> toResponseList(List<Category> categories);
}
