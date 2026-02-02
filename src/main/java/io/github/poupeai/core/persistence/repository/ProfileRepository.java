package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByEmail(String email);

    @Query("SELECT p FROM ProfileEntity p WHERE p.deactivated = true AND p.deactivationScheduledAt <= :currentTime")
    List<ProfileEntity> findExpiredDeactivatedProfiles(@Param("currentTime") OffsetDateTime currentTime);
}
