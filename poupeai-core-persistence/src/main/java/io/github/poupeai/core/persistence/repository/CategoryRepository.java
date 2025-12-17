package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    boolean existsByNameAndProfile_UserId(String name, UUID userId);
    boolean existsByNameAndProfile_UserIdAndIdNot(String name, UUID userId, UUID id);
    List<CategoryEntity> findAllByProfile_UserId(UUID userId);
}
