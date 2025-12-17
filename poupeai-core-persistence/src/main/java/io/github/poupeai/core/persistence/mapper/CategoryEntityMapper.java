package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.persistence.entity.CategoryEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    Category toDomain(CategoryEntity entity);

    @Mapping(target = "profile", ignore = true)
    CategoryEntity toEntity(Category domain);

    List<Category> toDomainList(List<CategoryEntity> entities);

    List<CategoryEntity> toEntityList(List<Category> domains);
}