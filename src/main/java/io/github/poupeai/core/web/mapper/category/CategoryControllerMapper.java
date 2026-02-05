package io.github.poupeai.core.web.mapper.category;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.web.dto.category.CategoryRequest;
import io.github.poupeai.core.web.dto.category.CategoryResponse;
import io.github.poupeai.core.web.dto.category.CategoryUpdateRequest;
import io.github.poupeai.core.web.dto.category.InternalCategoryResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryControllerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toDomain(CategoryRequest request, UUID profileId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDomainFromDto(CategoryUpdateRequest dto, @MappingTarget Category domain);

    CategoryResponse toResponse(Category domain);
    
    List<CategoryResponse> toResponseList(List<Category> categories);

    List<InternalCategoryResponse> toInternalResponseList(List<Category> categories);
}
