package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {
    boolean existsByNameAndProfile_UserId(String name, UUID userId);
    boolean existsByNameAndProfile_UserIdAndIdNot(String name, UUID userId, UUID id);
    List<CategoryEntity> findAllByProfile_UserId(UUID userId);
    Optional<CategoryEntity> findByIdAndProfile_UserId(UUID id, UUID userId);
    boolean existsByIdAndProfile_UserId(UUID id, UUID userId);
}
